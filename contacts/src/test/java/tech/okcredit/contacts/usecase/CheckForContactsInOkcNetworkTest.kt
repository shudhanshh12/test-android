package tech.okcredit.contacts.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.server.data.CheckedResponse
import tech.okcredit.contacts.server.data.Contact
import tech.okcredit.contacts.server.data.Next
import tech.okcredit.contacts.store.preference.ContactPreference

class CheckForContactsInOkcNetworkTest {

    private val localSource: ContactsLocalSource = mock()
    private val remoteSource: ContactsRemoteSource = mock()
    private val contactPreference: ContactPreference = mock()
    private val deviceRepository: DeviceRepository = mock()
    private val contactsTracker: ContactsTracker = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var checkForContactsInOkcNetwork: CheckForContactsInOkcNetwork

    private val contactListSize = 50
    private val startTime1 = 100L
    private val startTime2 = 200L
    private val startTime3 = 300L
    private val startTime4 = 400L
    private val startTime5 = 500L
    private val lastId1 = "last-id1"
    private val lastId2 = "last-id2"
    private val lastId3 = "last-id3"
    private val lastId4 = "last-id4"
    private val lastId5 = "last-id5"
    private val deviceId = "device-id"
    private val businessId = "business-id"
    private val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
    private val contactList = arrayListOf<Contact>()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        runBlocking {
            checkForContactsInOkcNetwork = CheckForContactsInOkcNetwork(
                { localSource },
                { remoteSource },
                { contactPreference },
                { deviceRepository },
                { contactsTracker },
                { getActiveBusinessId },
            )

            whenever(contactPreference.getStartTime()).thenReturn(startTime1)
            whenever(contactPreference.getLastId()).thenReturn(lastId1)
            whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            repeat(contactListSize) {
                contactList.add(
                    mock<Contact>().apply {
                        whenever(this.mobile).thenReturn("$it")
                        whenever(this.found).thenReturn(true)
                        whenever(this.type).thenReturn(1)
                    }
                )
            }
        }
    }

    @After
    fun verifyAfterTestCase() {
        runBlocking {
            verify(contactPreference).getStartTime()
            verify(contactPreference).getLastId()
            verify(deviceRepository).getDevice()
            verify(contactsTracker).trackDownloadStarted()
        }
    }

    @Test
    fun `Given hasMore is false in response getContactsInOkcNetwork should be called once`() {
        runBlocking {
            // Given
            val next = mock<Next>().apply {
                whenever(this.hasMore).thenReturn(false)
                whenever(this.startTimestamp).thenReturn(startTime2)
                whenever(this.lastId).thenReturn(lastId2)
            }
            val response = mock<CheckedResponse>().apply {
                whenever(this.contacts).thenReturn(contactList)
                whenever(this.next).thenReturn(next)
            }
            whenever(remoteSource.getCheckedContacts(any(), any(), any(), eq(businessId))).thenReturn(response)

            // When
            checkForContactsInOkcNetwork.execute()

            // Then
            verify(remoteSource).getCheckedContacts(deviceId, startTime1, lastId1, businessId)
            verify(localSource, times(contactListSize)).updateFoundStatusAndType(any(), any(), any())
            verify(contactPreference).setStartTime(startTime2)
            verify(contactPreference).setLastId(lastId2)
            verify(contactsTracker).trackDownloadComplete(50, 1)
        }
    }

    @Test
    fun `Given hasMore is true then false in response getContactsInOkcNetwork should be called twice`() {
        runBlocking {
            // Given
            val next = mock<Next>().apply {
                whenever(this.hasMore).thenReturn(true).thenReturn(false)
                whenever(this.startTimestamp).thenReturn(startTime2).thenReturn(startTime3)
                whenever(this.lastId).thenReturn(lastId2).thenReturn(lastId3)
            }
            val response = mock<CheckedResponse>().apply {
                whenever(this.contacts).thenReturn(contactList)
                whenever(this.next).thenReturn(next)
            }
            whenever(remoteSource.getCheckedContacts(any(), any(), any(), eq(businessId))).thenReturn(response)

            // When
            checkForContactsInOkcNetwork.execute()

            // Then
            verify(remoteSource).getCheckedContacts(deviceId, startTime1, lastId1, businessId)
            verify(remoteSource).getCheckedContacts(deviceId, startTime2, lastId2, businessId)
            verify(localSource, times(contactListSize * 2)).updateFoundStatusAndType(any(), any(), any())
            verify(contactPreference).setStartTime(startTime3)
            verify(contactPreference).setLastId(lastId3)
            verify(contactsTracker).trackDownloadComplete(100, 2)
        }
    }

    @Test
    fun `Given hasMore is true thrice then false in response getContactsInOkcNetwork should be called 4 times`() {
        runBlocking {
            // Given
            val next = mock<Next>().apply {
                whenever(this.hasMore).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false)
                whenever(this.startTimestamp).thenReturn(startTime2).thenReturn(startTime3).thenReturn(startTime4)
                    .thenReturn(startTime5)
                whenever(this.lastId).thenReturn(lastId2).thenReturn(lastId3).thenReturn(lastId4).thenReturn(lastId5)
            }
            val response = mock<CheckedResponse>().apply {
                whenever(this.contacts).thenReturn(contactList)
                whenever(this.next).thenReturn(next)
            }
            whenever(remoteSource.getCheckedContacts(any(), any(), any(), any())).thenReturn(response)

            // When
            checkForContactsInOkcNetwork.execute()

            // Then
            verify(remoteSource).getCheckedContacts(deviceId, startTime1, lastId1, businessId)
            verify(remoteSource).getCheckedContacts(deviceId, startTime2, lastId2, businessId)
            verify(remoteSource).getCheckedContacts(deviceId, startTime3, lastId3, businessId)
            verify(remoteSource).getCheckedContacts(deviceId, startTime4, lastId4, businessId)
            verify(localSource, times(contactListSize * 4)).updateFoundStatusAndType(any(), any(), any())
            verify(contactPreference).setStartTime(startTime5)
            verify(contactPreference).setLastId(lastId5)
            verify(contactsTracker).trackDownloadComplete(200, 4)
        }
    }
}
