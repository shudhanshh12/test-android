package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.ContactSync
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function3
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.contract.model.Contact
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncContactsWithAccount @Inject constructor(
    private val contactsRepository: Lazy<ContactsRepository>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            syncContactsWithAccount(_businessId)
        }
    }

    private fun syncContactsWithAccount(businessId: String): Completable {
        val updatedContactList = mutableListOf<ContactSync>()
        return Single.zip<List<Contact>, List<Customer>, List<Supplier>, List<Completable>>(
            contactsRepository.get().getContacts().firstOrError(),
            customerRepo.get().listCustomers(businessId).firstOrError(),
            supplierCreditRepository.get().getSuppliers(businessId).firstOrError(),
            Function3 { contactList, customerList, supplierList ->
                if (contactList.isEmpty()) {
                    return@Function3 mutableListOf()
                }
                if (customerList.isEmpty() && supplierList.isEmpty()) {
                    return@Function3 mutableListOf()
                }
                val selectedAccountList = mutableListOf<Completable>()
                updatedContactList.clear()
                val map = hashMapOf<String, Contact>()
                contactList.associateByTo(map) { it.mobile }
                customerList.forEach { customer ->
                    if (map.containsKey(customer.mobile)) {
                        val contact = map[customer.mobile]!!
                        if (contact.mobile == customer.mobile &&
                            contact.name != customer.description &&
                            customer.isRestrictContactSync().not()
                        ) {
                            updatedContactList.add(
                                ContactSync(
                                    customer.id,
                                    contact.name
                                )
                            )
                            selectedAccountList.add(
                                customerRepo.get().updateDescription(contact.name, customer.id, businessId)
                            )
                        }
                    }
                }
                supplierList.forEach { supplier ->
                    if (map.containsKey(supplier.mobile)) {
                        val contact = map[supplier.mobile]!!
                        if (contact.mobile == supplier.mobile &&
                            contact.name != supplier.name &&
                            supplier.restrictContactSync.not()
                        ) {
                            updatedContactList.add(
                                ContactSync(
                                    supplier.id,
                                    contact.name
                                )
                            )
                            selectedAccountList.add(
                                supplierCreditRepository.get().updateSupplerName(
                                    contact.name,
                                    supplier.id
                                )
                            )
                        }
                    }
                }
                return@Function3 selectedAccountList
            }
        ).onErrorReturn {
            RecordException.recordException(it)
            mutableListOf()
        }.flatMapCompletable {
            if (it.isEmpty()) {
                Completable.complete()
            } else Completable.concat(it)
                .andThen(remoteSource.get().syncUpdatedAccounts(updatedContactList, businessId))
        }
    }

    fun schedule(businessId: String): Completable {
        val contactSyncer = firebaseRemoteConfig.get().getBoolean(CONTACT_SYNCER_FLAG_KEY)
        if (contactSyncer) {
            return Completable
                .fromAction {
                    val workCategory = "sync_contacts_with_accounts"
                    val workName = "sync_contacts_with_accounts"
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                        .addTag(workCategory)
                        .addTag(workName)
                        .setInputData(
                            workDataOf(
                                Worker.BUSINESS_ID to businessId
                            )
                        )
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                        .build()
                    LogUtils.enableWorkerLogging(workRequest)
                    workManager.get()
                        .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
                }
                .subscribeOn(ThreadUtils.newThread())
        } else {
            return Completable.complete()
        }
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncContactsWithAccount: Lazy<SyncContactsWithAccount>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business-id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return syncContactsWithAccount.get().execute(businessId).onErrorComplete()
        }

        class Factory @Inject constructor(private val syncContactsWithAccount: Lazy<SyncContactsWithAccount>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncContactsWithAccount)
            }
        }
    }

    companion object {
        val CONTACT_SYNCER_FLAG_KEY = "contact_syncer_flag"
    }
}
