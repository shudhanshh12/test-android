package tech.okcredit.contacts.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.base.permission.Permission
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.contract.ContactsRepository
import javax.inject.Inject

/**
 *  This class is used to save OkCredit Contact to users device received from server getOkCreditContact]
 *
 *  1. if permission available [permissionAvailable], then we schedule worker to add contact to device in background
 *
 *  2. if permission not available, then we don't disturb user by asking permission. We simple return getOkCreditContact]
 *   values to [@link AddOkCreditContactInAppBottomSheet] which opens device screen to save contact [openSaveContactDeviceScreen]
 *
 */

class AddOkCreditContact @Inject constructor(
    private val context: Lazy<Context>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val remoteSource: Lazy<ContactsRemoteSource>
) {

    var contactAdded = false
    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(
            if (permissionAvailable()) {
                contactAdded = true
                contactsRepository.get().scheduleAddOkCreditContactToUserDevice()
                    .andThen(Single.just(contactAdded))
            } else {
                contactAdded = false
                Single.just(contactAdded)
            }
        )
    }

    private fun permissionAvailable() = Permission.isContactWritePermissionAlreadyGranted(context.get())
}
