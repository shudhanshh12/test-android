package tech.okcredit.contacts.worker

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts.DISPLAY_NAME
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.server.OkCreditContactResponse
import tech.okcredit.contacts.ui.AddOkCreditContactInAppBottomSheet.Companion.DEFAULT_CONTACT_NAME
import tech.okcredit.contacts.usecase.InsertContactIntoPhoneBook
import tech.okcredit.feature_help.contract.GetSupportNumber
import javax.inject.Inject

class AddOkCreditContactsWorker constructor(
    private val context: Context,
    params: WorkerParameters,
    private val remoteSource: ContactsRemoteSource,
    private val contactsRepository: ContactsRepository,
    private val contactsTracker: ContactsTracker,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val insertContactIntoPhoneBook: Lazy<InsertContactIntoPhoneBook>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : RxWorker(context, params) {

    override fun createWork(): Single<Result> {
        return saveOkCreditContactToUserDevice()
            .andThen(Single.just(Result.success()))
            .onErrorResumeNext {
                Single.just(Result.retry())
            }
    }

    private fun saveOkCreditContactToUserDevice(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            getOkCreditContact(businessId)
                .flatMapCompletable { okCreditContact ->
                    if (isNumberAlreadyInPhoneBook(okCreditContact.okc_number)) {
                        contactsTracker.trackOkCreditContactAlreadyExist()
                        Completable.complete()
                    } else {
                        saveContactToDevice(okCreditContact.okc_name, okCreditContact.okc_number)
                            .andThen(contactsRepository.scheduleAcknowledgeContactSaved())
                            .doOnComplete { contactsTracker.trackOkCreditContactSaved(ContactsTracker.PropertyValue.AUTO) }
                    }
                }
        }
    }

    private fun isNumberAlreadyInPhoneBook(number: String): Boolean {
        val projection = arrayOf(DISPLAY_NAME)
        val uri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
        } catch (e: Exception) {
            RecordException.recordException(e)
        } finally {
            val isPresent = cursor != null && cursor.count > 0
            cursor?.close()
            return isPresent
        }
    }

    private fun getOkCreditContact(businessId: String): Single<OkCreditContactResponse> {
        return remoteSource.getOkCreditContact(businessId)
            .onErrorReturn { OkCreditContactResponse(DEFAULT_CONTACT_NAME, getSupportNumber.get().supportNumber) }
    }

    private fun saveContactToDevice(name: String, mobile: String) =
        insertContactIntoPhoneBook.get().execute(name, mobile)

    class Factory @Inject constructor(
        private val remoteSource: Lazy<ContactsRemoteSource>,
        private val contactsRepository: Lazy<ContactsRepository>,
        private val contactsTracker: Lazy<ContactsTracker>,
        private val getSupportNumber: Lazy<GetSupportNumber>,
        private val insertContactIntoPhoneBook: Lazy<InsertContactIntoPhoneBook>,
        private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return AddOkCreditContactsWorker(
                context,
                params,
                remoteSource.get(),
                contactsRepository.get(),
                contactsTracker.get(),
                getSupportNumber,
                insertContactIntoPhoneBook,
                getActiveBusinessId
            )
        }
    }
}
