package tech.okcredit.contacts

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import org.junit.Test
import org.mockito.Mockito.`when`
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.contacts.store.preference.ContactPreference
import tech.okcredit.contacts.usecase.GetContactsAndSyncWithPhoneBook
import tech.okcredit.contacts.worker.CheckForContactsInOkcNetworkWorker
import tech.okcredit.contacts.worker.UploadContactsWorker

class ContactsRepositoryTest {

    private val localSource: ContactsLocalSource = mock()
    private val contactPreference: ContactPreference = mock()
    private val okcWorkManager: OkcWorkManager = mock()
    private val uploadContactsWorker: UploadContactsWorker = mock()
    private val checkForContactsInOkcNetworkWorker: CheckForContactsInOkcNetworkWorker = mock()
    private val getContactsAndSyncWithPhoneBook: GetContactsAndSyncWithPhoneBook = mock()
    private val contactsRepository = ContactsRepositoryImpl(
        { localSource },
        { okcWorkManager },
        { contactPreference },
        { uploadContactsWorker },
        { checkForContactsInOkcNetworkWorker },
        { getContactsAndSyncWithPhoneBook }
    )

    @Test
    fun `should return contacts list on getContacts()`() {
        `when`(localSource.getContacts()).thenReturn(Observable.just(TestData.TEST_CONTACTS))
        val testObserver = contactsRepository.getContacts().test()

        // expectations
        assertThat(testObserver.values().first() == TestData.TEST_CONTACTS).isTrue()
        testObserver.dispose()
    }

    @Test
    fun `should call getContacts() inside store`() {
        `when`(localSource.getContacts()).thenReturn(Observable.just(TestData.TEST_CONTACTS))
        val testObserver = contactsRepository.getContacts().test()

        // expectations
        verify(localSource).getContacts()
        testObserver.dispose()
    }
}
