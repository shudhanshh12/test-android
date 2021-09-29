package tech.okcredit.contacts.usecase

import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.contract.ContactsRepository
import javax.inject.Inject

class GetContactsAndSyncWithPhoneBook @Inject constructor(
    private val contactsRepository: Lazy<ContactsRepository>,
    private val updateLocalContacts: Lazy<UpdateLocalContacts>,
    private val context: Lazy<Context>,
) {
    fun execute(searchQuery: String? = null): Observable<ContactsRepository.GetContactsResponse> {
        val permissionGranted = Permission.isContactPermissionAlreadyGranted(context.get())
        return if (permissionGranted) {
            // Contacts present in database will be emitted immediately, Parallelly UpdateLocalContacts is triggered to
            // keep database contacts in sync with phone book contacts
            Observable.merge(
                updateContactsInLocal().toObservable(),
                contactsRepository.get().getContacts(searchQuery)
            ).map { ContactsRepository.GetContactsResponse(it, true) }
        } else {
            contactsRepository.get().getContacts(searchQuery)
                .map { ContactsRepository.GetContactsResponse(it, false) }
        }
    }

    private fun updateContactsInLocal() = rxCompletable { updateLocalContacts.get().execute() }.onErrorComplete()
}
