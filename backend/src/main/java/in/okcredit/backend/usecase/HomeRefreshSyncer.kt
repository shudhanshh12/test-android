package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.contacts.contract.ContactsRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Triggers : pull-to-refresh on home screen
 * Rate limit : none
 */
class HomeRefreshSyncer @Inject constructor(
    private val checkAuth: Lazy<CheckAuth>,
    private val businessApi: Lazy<BusinessRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val ab: Lazy<AbRepository>,
    private val syncCustomersImpl: Lazy<SyncCustomersImpl>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val dueInfoSyncer: Lazy<DueInfoSyncer>,
    private val serverActionableChecker: Lazy<ServerActionableChecker>,
    private val syncCustomerTxnAlert: Lazy<SyncCustomerTxnAlert>,
    private val dynamicViewRepository: Lazy<DynamicViewRepository>,
    private val inAppNotificationRepository: Lazy<InAppNotificationRepository>,
    private val syncIndividual: Lazy<SyncIndividual>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val contactsRepository: Lazy<ContactsRepository>,
) {
    companion object {
        @NonNls
        const val SOURCE = "home_refresh"
    }

    fun execute(source: String): Completable {
        return isUserLoggedIn()
            .flatMapCompletable { isUserLoggedIn ->
                if (isUserLoggedIn) {
                    getActiveBusinessId.get().execute().flatMapCompletable { _businessId ->
                        Completable.mergeArray(
                            rxCompletable { syncIndividual.get().syncIndividualAndNewBusinessesIfPresent() },
                            syncBusiness(_businessId),
                            syncCustomersImpl.get().execute(_businessId),
                            syncTransactionsImpl.get().execute(source, businessId = _businessId),
                            Completable.fromAction {
                                collectionSyncer.get().scheduleSyncEverything(source, businessId = _businessId)
                            },
                            supplierCreditRepository.get().syncSupplierEnabledCustomerIds(_businessId),
                            supplierCreditRepository.get().syncEverything(_businessId),
                            ab.get().sync(_businessId, "home_refresh").onErrorComplete(),
                            dueInfoSyncer.get().schedule(_businessId),
                            serverActionableChecker.get().schedule(_businessId),
                            syncCustomerTxnAlert.get().schedule(_businessId),
                            dynamicViewRepository.get().scheduleSyncCustomizations(businessId = _businessId),
                            inAppNotificationRepository.get().scheduleSyncCompletable(_businessId),
                            contactsRepository.get().scheduleUploadContactsWorker(skipRateLimit = true),
                            contactsRepository.get().scheduleCheckForContactsInOkcNetwork(skipRateLimit = true),
                        )
                    }
                } else {
                    Completable.complete()
                }
            }.doOnComplete {
                Timber.d("<<<HomeRefreshSyncer completed")
            }.doOnError {
                RecordException.recordException(it)
                Timber.e("<<<<Worker Error HomeRefreshSyncer error ${it.message}")
            }
    }

    private fun isUserLoggedIn() = checkAuth.get().execute().firstOrError()

    private fun syncBusiness(businessId: String): Completable {
        return businessApi.get().executeSyncBusiness(businessId)
    }
}
