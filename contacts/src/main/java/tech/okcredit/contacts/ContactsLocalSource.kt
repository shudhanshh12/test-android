package tech.okcredit.contacts

import io.reactivex.Observable
import tech.okcredit.contacts.contract.model.Contact

interface ContactsLocalSource {
    fun getContacts(): Observable<List<Contact>>

    fun getFilteredContacts(searchQuery: String): Observable<List<Contact>>

    suspend fun getUnsyncedContacts(pagingLimit: Int): List<Contact>

    suspend fun insertContacts(contacts: List<Contact>)

    suspend fun deleteContacts(mobileList: List<String>)

    suspend fun updateSyncStatus(mobileList: List<String>, synced: Boolean)

    suspend fun updateFoundStatusAndType(mobile: String, found: Boolean, type: Int)

    suspend fun getLastUpdatedTimestamp(): Long?

    suspend fun getContactMobileNumbers(): List<String>
}
