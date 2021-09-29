package tech.okcredit.contacts.contract

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.contacts.contract.model.Contact

interface ContactsRepository {
    fun getContacts(searchQuery: String? = null): Observable<List<Contact>>

    fun getContactsAndSyncWithPhonebook(searchQuery: String? = null): Observable<GetContactsResponse>

    fun showAddOkCreditContactInApp(): Single<Boolean>

    fun scheduleAddOkCreditContactToUserDevice(): Completable

    fun scheduleAcknowledgeContactSaved(): Completable

    fun scheduleCheckForContactsInOkcNetwork(skipRateLimit: Boolean = false, workerName: String? = null): Completable

    fun scheduleUploadContactsWorker(skipRateLimit: Boolean = false): Completable

    fun clearLocalData(): Completable

    suspend fun setContactInAppDisplayed(showed: Boolean)

    data class GetContactsResponse(val contactList: List<Contact>, val isPermissionGranted: Boolean)
}
