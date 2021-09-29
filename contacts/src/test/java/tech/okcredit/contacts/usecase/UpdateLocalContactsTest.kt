package tech.okcredit.contacts.usecase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.contract.model.Contact

class UpdateLocalContactsTest {

    private val getPhoneBookContacts: GetPhoneBookContacts = mock()
    private val localSource: ContactsLocalSource = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val contactsTracker: ContactsTracker = mock()
    private lateinit var updateLocalContacts: UpdateLocalContacts

    private val contact1: Contact = mock()
    private val contact2: Contact = mock()
    private val contact3: Contact = mock()
    private val contact4: Contact = mock()
    private val contact5: Contact = mock()

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        runBlocking {
            whenever(firebaseRemoteConfig.getLong("contact_sync_chunk_size")).thenReturn(500)
            updateLocalContacts = UpdateLocalContacts(
                { getPhoneBookContacts },
                { localSource },
                firebaseRemoteConfig,
                { contactsTracker },
            )
            setupTestData()
        }
    }

    private fun setupTestData() {
        listOf(contact1, contact2, contact3, contact4, contact5).onEachIndexed { index, contact ->
            whenever(contact.timestamp).thenReturn(1110L + index)
            whenever(contact.mobile).thenReturn("9876500000$index")
        }
    }

    @Test
    fun `given database table is empty & phonebook is empty then do not insert any contacts`() {
        runBlocking {
            // Given
            whenever(getPhoneBookContacts.execute()).thenReturn(emptyList())
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(0)
            whenever(localSource.getContactMobileNumbers()).thenReturn(emptyList())

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource, times(0)).insertContacts(any())
            verify(localSource, times(0)).deleteContacts(any())
        }
    }

    @Test
    fun `given database table is empty & phonebook has 3 new contacts then insert 3 contacts`() {
        runBlocking {
            // Given
            val contactList = listOf(contact1, contact2, contact3)
            whenever(getPhoneBookContacts.execute()).thenReturn(contactList)
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(0)
            whenever(localSource.getContactMobileNumbers()).thenReturn(emptyList())

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource).insertContacts(contactList)
            verify(localSource, times(0)).deleteContacts(any())
        }
    }

    @Test
    fun `given database table has 3 contacts & phonebook has 5 (2 new) contacts then insert 2 contacts`() {
        runBlocking {
            // Given
            val phoneBookContactList = listOf(contact1, contact2, contact3, contact4, contact5)
            val databaseContactList = listOf(contact3.mobile, contact2.mobile, contact1.mobile)
            whenever(getPhoneBookContacts.execute()).thenReturn(phoneBookContactList)
            val timestamp = contact3.timestamp
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(timestamp)
            whenever(localSource.getContactMobileNumbers()).thenReturn(databaseContactList)

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource).insertContacts(listOf(contact4, contact5))
            verify(localSource, times(0)).deleteContacts(any())
        }
    }

    @Test
    fun `given database table has 3 contacts & phonebook has same 3 contacts then do not insert or delete`() {
        runBlocking {
            // Given
            val phoneBookContactList = listOf(contact1, contact2, contact3)
            val databaseContactList = listOf(contact3.mobile, contact2.mobile, contact1.mobile)
            whenever(getPhoneBookContacts.execute()).thenReturn(phoneBookContactList)
            val timestamp = contact3.timestamp
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(timestamp)
            whenever(localSource.getContactMobileNumbers()).thenReturn(databaseContactList)

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource, times(0)).insertContacts(any())
            verify(localSource, times(0)).deleteContacts(any())
        }
    }

    @Test
    fun `given database table has 3 contacts & phonebook has only 1 contact then delete 2 contacts`() {
        runBlocking {
            // Given
            val phoneBookContactList = listOf(contact1)
            val databaseContactList = listOf(contact3.mobile, contact2.mobile)
            whenever(getPhoneBookContacts.execute()).thenReturn(phoneBookContactList)
            val timestamp = contact3.timestamp
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(timestamp)
            whenever(localSource.getContactMobileNumbers()).thenReturn(databaseContactList)

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource, times(0)).insertContacts(any())
            verify(localSource).deleteContacts(listOf(contact3.mobile, contact2.mobile))
        }
    }

    @Test
    fun `given database table has 3 contacts & phonebook has 2 (1 new) contact then insert 1 contact & delete 2 contacts`() {
        runBlocking {
            // Given
            val phoneBookContactList = listOf(contact1, contact4)
            val databaseContactList = listOf(contact3.mobile, contact2.mobile, contact1.mobile)
            whenever(getPhoneBookContacts.execute()).thenReturn(phoneBookContactList)
            val timestamp = contact3.timestamp
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(timestamp)
            whenever(localSource.getContactMobileNumbers()).thenReturn(databaseContactList)

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource).insertContacts(listOf(contact4))
            verify(localSource).deleteContacts(listOf(contact3.mobile, contact2.mobile))
        }
    }

    @Test
    fun `given database table has 3 contacts & phonebook has 2+1 contact then insert 2 contacts& delete 1 contact`() {
        runBlocking {
            // Given
            val phoneBookContactList = listOf(contact1, contact4, contact5)
            val databaseContactList = listOf(contact3.mobile, contact2.mobile, contact1.mobile)
            whenever(getPhoneBookContacts.execute()).thenReturn(phoneBookContactList)
            val timestamp = contact3.timestamp
            whenever(localSource.getLastUpdatedTimestamp()).thenReturn(timestamp)
            whenever(localSource.getContactMobileNumbers()).thenReturn(databaseContactList)

            // When
            updateLocalContacts.execute()

            // Then
            verify(localSource).getLastUpdatedTimestamp()
            verify(getPhoneBookContacts).execute()
            verify(localSource).insertContacts(listOf(contact4, contact5))
            verify(localSource).deleteContacts(listOf(contact3.mobile, contact2.mobile))
        }
    }
}
