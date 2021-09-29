package tech.okcredit.contacts.store

import androidx.room.EmptyResultSetException
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.encloseWithPercentageSymbol
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.contacts.store.database.ContactsDataBaseDao
import javax.inject.Inject
import tech.okcredit.contacts.store.database.Contact as DbContact

class ContactsLocalSourceImpl @Inject constructor(
    private val contactsDataBaseDao: Lazy<ContactsDataBaseDao>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
) : ContactsLocalSource {

    override fun getContacts(): Observable<List<Contact>> {
        return contactsDataBaseDao.get().getContacts()
            .subscribeOn(schedulerProvider.get().io())
            .map { dbContactList -> dbContactList.map { it.toModelContact() } }
    }

    override suspend fun insertContacts(contacts: List<Contact>) {
        val list = contacts.map { it.toDbContact() }.toTypedArray()
        return contactsDataBaseDao.get().insertContacts(*list)
    }

    override suspend fun deleteContacts(mobileList: List<String>) = contactsDataBaseDao.get().deleteContacts(mobileList)

    override fun getFilteredContacts(searchQuery: String): Observable<List<Contact>> {
        return contactsDataBaseDao.get().getFilteredContacts(searchQuery.encloseWithPercentageSymbol())
            .subscribeOn(schedulerProvider.get().io())
            .map { dbContactList -> dbContactList.map { it.toModelContact() } }
    }

    override suspend fun getUnsyncedContacts(pagingLimit: Int): List<Contact> {
        return contactsDataBaseDao.get().getUnsyncedContacts(pagingLimit)
            .map { it.toModelContact() }
    }

    override suspend fun updateSyncStatus(mobileList: List<String>, synced: Boolean) {
        contactsDataBaseDao.get().updateSyncStatus(mobileList, synced)
    }

    override suspend fun updateFoundStatusAndType(mobile: String, found: Boolean, type: Int) {
        contactsDataBaseDao.get().updateFoundStatusAndType(mobile, found, type)
    }

    override suspend fun getLastUpdatedTimestamp(): Long? {
        return try {
            contactsDataBaseDao.get().getLastUpdatedTimestamp()
        } catch (e: EmptyResultSetException) { // No contacts present in database table
            return null
        }
    }

    override suspend fun getContactMobileNumbers(): List<String> = contactsDataBaseDao.get().getContactMobileNumbers()

    private fun Contact.toDbContact() = DbContact(
        phoneBookId = this.phonebookId,
        name = this.name,
        mobile = this.mobile,
        picUri = this.picUri,
        found = this.found,
        timestamp = this.timestamp,
        synced = this.synced,
        type = this.type
    )

    private fun DbContact.toModelContact() = Contact(
        phonebookId = this.phoneBookId,
        name = this.name ?: "",
        mobile = this.mobile,
        picUri = this.picUri,
        found = this.found,
        timestamp = this.timestamp,
        synced = this.synced,
        type = this.type
    )
}
