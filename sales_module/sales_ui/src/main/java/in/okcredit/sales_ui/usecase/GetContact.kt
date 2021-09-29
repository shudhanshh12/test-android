package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.contract.model.Contact
import javax.inject.Inject

class GetContact @Inject constructor(
    private val context: Lazy<Context>,
    private val contactsRepository: Lazy<ContactsRepository>,
) : UseCase<String?, GetContact.Response> {

    override fun execute(searchQuery: String?): Observable<Result<Response>> {
        val permissionGranted = Permission.isContactPermissionAlreadyGranted(context.get())
        return UseCase.wrapObservable(
            contactsRepository.get().getContactsAndSyncWithPhonebook(searchQuery).map { (contacts) ->
                return@map Response(contacts, permissionGranted, searchQuery)
            }
        )
    }

    data class Response(
        val contacts: List<Contact>,
        val isPermissionAllowed: Boolean,
        val searchQuery: String?,
    )
}
