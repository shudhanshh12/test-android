package tech.okcredit.contacts

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.contacts.store.preference.ContactPreference
import tech.okcredit.contacts.usecase.GetContactsAndSyncWithPhoneBook
import tech.okcredit.contacts.worker.AcknowledgeContactSavedWorker
import tech.okcredit.contacts.worker.AddOkCreditContactsWorker
import tech.okcredit.contacts.worker.CheckForContactsInOkcNetworkWorker
import tech.okcredit.contacts.worker.UploadContactsWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsRepositoryImpl @Inject constructor(
    private val localSource: Lazy<ContactsLocalSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val contactPreference: Lazy<ContactPreference>,
    private val uploadContactsWorker: Lazy<UploadContactsWorker>,
    private val checkForContactsInOkcNetworkWorker: Lazy<CheckForContactsInOkcNetworkWorker>,
    private val getContactsAndSyncWithPhoneBook: Lazy<GetContactsAndSyncWithPhoneBook>,
) : ContactsRepository {

    companion object {
        const val WORKER_PROCESS_ADD_OKCREDIT_CONTACT = "contacts/addOkCreditContact"
        const val WORKER_PROCESS_ACKNOWLEDGE_CONTACT_SAVED = "contacts/acknowledgeContactSaved"
    }

    override fun getContacts(searchQuery: String?): Observable<List<Contact>> {
        return if (searchQuery.isNullOrBlank()) localSource.get().getContacts()
        else localSource.get().getFilteredContacts(searchQuery)
    }

    override fun getContactsAndSyncWithPhonebook(searchQuery: String?) =
        getContactsAndSyncWithPhoneBook.get().execute(searchQuery)

    override fun showAddOkCreditContactInApp(): Single<Boolean> {
        return rxSingle {
            contactPreference.get().canShowContactInApp()
        }.subscribeOn(ThreadUtils.io())
    }

    override fun scheduleAddOkCreditContactToUserDevice(): Completable {
        return Completable.fromAction {
            val tag = WORKER_PROCESS_ADD_OKCREDIT_CONTACT
            val workRequest = OneTimeWorkRequestBuilder<AddOkCreditContactsWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(tag)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(
                    WORKER_PROCESS_ADD_OKCREDIT_CONTACT,
                    Scope.Individual,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }.subscribeOn(ThreadUtils.newThread())
    }

    override fun scheduleAcknowledgeContactSaved(): Completable {
        return Completable.fromAction {
            val tag = WORKER_PROCESS_ACKNOWLEDGE_CONTACT_SAVED
            val workRequest = OneTimeWorkRequestBuilder<AcknowledgeContactSavedWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(tag)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(
                    WORKER_PROCESS_ACKNOWLEDGE_CONTACT_SAVED,
                    Scope.Individual,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }.subscribeOn(ThreadUtils.newThread())
    }

    override fun clearLocalData() = rxCompletable { contactPreference.get().clear() }

    override fun scheduleCheckForContactsInOkcNetwork(skipRateLimit: Boolean, workerName: String?) =
        checkForContactsInOkcNetworkWorker.get().schedule(skipRateLimit, workerName)

    override fun scheduleUploadContactsWorker(skipRateLimit: Boolean) =
        uploadContactsWorker.get().schedule(skipRateLimit)

    override suspend fun setContactInAppDisplayed(showed: Boolean) =
        contactPreference.get().setContactInappDisplayed(showed)
}
