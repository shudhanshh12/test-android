package tech.okcredit.contacts.usecase

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.contract.model.Contact

class GetContactsAndSyncWithPhoneBookTest {

    private val contactsRepository: ContactsRepository = mock()
    private val updateLocalContacts: UpdateLocalContacts = mock()
    private val context: Context = mock()
    private lateinit var getContactsAndSyncWithPhoneBook: GetContactsAndSyncWithPhoneBook

    @Before
    fun setup() {
        getContactsAndSyncWithPhoneBook =
            GetContactsAndSyncWithPhoneBook(
                { contactsRepository },
                { updateLocalContacts },
                { context },
            )
        mockkObject(Permission)
    }

    @Test
    fun `Given contact permission is not granted then return contact list from local`() {
        runBlocking {
            // Given
            val contactList: List<Contact> = mock()
            every { Permission.isContactPermissionAlreadyGranted(context) } returns false
            whenever(contactsRepository.getContacts()).thenReturn(Observable.just(contactList))

            // When
            val testObserver = getContactsAndSyncWithPhoneBook.execute().test()

            // Then
            testObserver.assertValue(ContactsRepository.GetContactsResponse(contactList, false))
            verify(contactsRepository).getContacts()
            verify(updateLocalContacts, times(0)).execute()
            testObserver.dispose()
        }
    }

    @Test
    fun `Given contact permission is granted then return contact list`() {
        runBlocking {
            // Given
            val contactList: List<Contact> = mock()
            every { Permission.isContactPermissionAlreadyGranted(context) } returns true
            whenever(contactsRepository.getContacts()).thenReturn(Observable.just(contactList))

            // When
            val testObserver = getContactsAndSyncWithPhoneBook.execute().test()
            testObserver.awaitCount(2)

            // Then
            testObserver.assertValue(ContactsRepository.GetContactsResponse(contactList, true))
            verify(contactsRepository).getContacts()
            verify(updateLocalContacts).execute()
            testObserver.dispose()
        }
    }
}
