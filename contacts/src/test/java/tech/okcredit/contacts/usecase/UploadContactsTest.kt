package tech.okcredit.contacts.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.device.Device
import `in`.okcredit.merchant.device.DeviceRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
import tech.okcredit.contacts.contract.model.Contact

class UploadContactsTest {

    private val localSource: ContactsLocalSource = mock()
    private val remoteSource: ContactsRemoteSource = mock()
    private val deviceRepository: DeviceRepository = mock()
    private val contactsTracker: ContactsTracker = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var uploadContacts: UploadContacts

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        runBlocking {
            whenever(firebaseRemoteConfig.getLong("contact_sync_paging_limit")).thenReturn(1000)
            whenever(firebaseRemoteConfig.getLong("contact_sync_chunk_size")).thenReturn(500)
            uploadContacts = UploadContacts(
                { localSource },
                { remoteSource },
                { deviceRepository },
                { contactsTracker },
                firebaseRemoteConfig,
                { getActiveBusinessId }
            )
        }
    }

    @After
    fun verifyAfterTestCase() {
        runBlocking {
            verify(contactsTracker).trackUploadStarted()
        }
    }

    private fun getSampleContactsList(size: Int): List<Contact> {
        val contactList = arrayListOf<Contact>()
        repeat(size) { contactList.add(mock<Contact>().apply { whenever(this.mobile).thenReturn("$it") }) }
        return contactList
    }

    @Test
    fun `given no unsynced contacts present then uploadContacts, updateSyncStatus should not be called`() {
        runBlocking {
            // Given
            val device = mock<Device>().apply { whenever(this.id).thenReturn("id") }
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
            whenever(localSource.getUnsyncedContacts(any())).thenReturn(emptyList())
            // When
            uploadContacts.execute()

            // Then
            verify(deviceRepository).getDevice()
            verify(localSource).getUnsyncedContacts(1000)
            verify(remoteSource, times(0)).uploadContacts(any(), any(), any(), eq(businessId))
            verify(localSource, times(0)).updateSyncStatus(any(), any())
            verify(contactsTracker).trackUploadComplete(0, 0)
        }
    }

    @Test
    fun `given 500 contacts present then uploadContacts & updateSyncStatus should be called 1 & 1 time`() {
        runBlocking {
            // Given
            val contactList = getSampleContactsList(500)
            val deviceId = "id"
            val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
            whenever(localSource.getUnsyncedContacts(any())).thenReturn(contactList).thenReturn(emptyList())

            // When
            uploadContacts.execute()

            // Then
            verify(deviceRepository).getDevice()
            verify(localSource).getUnsyncedContacts(1000)
            verify(remoteSource).uploadContacts(deviceId, contactList, true, businessId)
            verify(localSource).updateSyncStatus(any(), eq(true))
            verify(contactsTracker).trackUploadComplete(500, 1)
        }
    }

    @Test
    fun `given 999 contacts present then uploadContacts & updateSyncStatus should be called 1 & 2 times`() {
        runBlocking {
            // Given
            val contactList = getSampleContactsList(999)
            val deviceId = "id"
            val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
            whenever(localSource.getUnsyncedContacts(any())).thenReturn(contactList).thenReturn(emptyList())

            // When
            uploadContacts.execute()

            // Then
            verify(deviceRepository).getDevice()
            verify(localSource).getUnsyncedContacts(1000)
            verify(remoteSource).uploadContacts(deviceId, contactList, true, businessId)
            verify(localSource, times(2)).updateSyncStatus(any(), eq(true))
            verify(contactsTracker).trackUploadComplete(999, 1)
        }
    }

    @Test
    fun `given 1001 contacts present then uploadContacts & updateSyncStatus should be called 2 & 3 times`() {
        runBlocking {
            // Given
            val contactList = getSampleContactsList(1001)
            val deviceId = "id"
            val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
            whenever(localSource.getUnsyncedContacts(any()))
                .thenReturn(contactList.subList(0, 1000))
                .thenReturn(contactList.subList(1000, 1001))

            // When
            uploadContacts.execute()

            // Then
            verify(deviceRepository).getDevice()
            verify(localSource, times(2)).getUnsyncedContacts(1000)
            verify(remoteSource, times(2)).uploadContacts(eq(deviceId), any(), any(), eq(businessId))
            verify(localSource, times(3)).updateSyncStatus(any(), eq(true))
            verify(contactsTracker).trackUploadComplete(1001, 2)
        }
    }

    @Test
    fun `given 2500 contacts present then uploadContacts & updateSyncStatus should be called 3 & 5 times`() {
        runBlocking {
            // Given
            val contactList = getSampleContactsList(2500)
            val deviceId = "id"
            val device = mock<Device>().apply { whenever(this.id).thenReturn(deviceId) }
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(deviceRepository.getDevice()).thenReturn(Observable.just(device))
            whenever(localSource.getUnsyncedContacts(any()))
                .thenReturn(contactList.subList(0, 1000))
                .thenReturn(contactList.subList(1000, 2000))
                .thenReturn(contactList.subList(2000, 2500))

            // When
            uploadContacts.execute()

            // Then
            verify(deviceRepository).getDevice()
            verify(localSource, times(3)).getUnsyncedContacts(1000)
            verify(remoteSource, times(3)).uploadContacts(eq(deviceId), any(), any(), eq(businessId))
            verify(localSource, times(5)).updateSyncStatus(any(), eq(true))
            verify(contactsTracker).trackUploadComplete(2500, 3)
        }
    }
}
