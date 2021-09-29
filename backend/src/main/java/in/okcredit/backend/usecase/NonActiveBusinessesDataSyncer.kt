package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase.SuggestedCustomersForAddTransactionShortcutSyncer
import `in`.okcredit.backend._offline.usecase.SyncContactsWithAccount
import `in`.okcredit.backend._offline.usecase.SyncCustomerTxnAlert
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.CollectionSyncer.Companion.SYNC_ALL
import `in`.okcredit.collection.contract.CollectionSyncer.Source.NON_ACTIVE_BUSINESSES_SYNCER
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.bills.BillRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * [NonActiveBusinessesDataSyncer] includes data sync calls for non-active businesses.
 *
 * Trigger : home screen open
 * Rate limit : once in 24 hours
 */
class NonActiveBusinessesDataSyncer @Inject constructor(
    private val checkAuth: Lazy<CheckAuth>,
    private val businessApi: Lazy<BusinessRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val ab: Lazy<AbRepository>,
    private val syncContactsWithAccount: Lazy<SyncContactsWithAccount>,
    private val syncCustomerTxnAlert: Lazy<SyncCustomerTxnAlert>,
    private val inAppNotificationRepository: Lazy<InAppNotificationRepository>,
    private val suggestedCustomersForAddTransactionShortcutSyncer: Lazy<SuggestedCustomersForAddTransactionShortcutSyncer>,
    private val billRepository: Lazy<BillRepository>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val syncCustomersImpl: Lazy<SyncCustomersImpl>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val dueInfoSyncer: Lazy<DueInfoSyncer>,
    private val serverActionableChecker: Lazy<ServerActionableChecker>,
    private val dynamicViewRepository: Lazy<DynamicViewRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Completable {
        return isUserLoggedIn()
            .flatMapCompletable { isUserLoggedIn ->
                if (isUserLoggedIn) {
                    getBusinessIdListWithoutActiveBusinessId()
                        .flatMapCompletable { nonActiveBusinessIdsList ->
                            Completable.concat(nonActiveBusinessIdsList.map { businessId -> sync(businessId) })
                        }
                } else {
                    Completable.complete()
                }
            }.doOnComplete {
                Timber.d("<<<NonActiveBusinessesDataSyncer completed")
            }.doOnError {
                RecordException.recordException(it)
                Timber.e("<<<<Worker Error NonActiveBusinessesDataSyncer error ${it.message}")
            }
    }

    private fun getBusinessIdListWithoutActiveBusinessId(): Single<List<String>> {
        return getActiveBusinessId.get().execute()
            .flatMap { activeBusinessId ->
                getBusinessIdList.get().execute().asObservable().firstOrError()
                    .map { businessIdList -> businessIdList - activeBusinessId }
            }
    }

    private fun sync(businessId: String): Completable {
        return Completable.mergeArray(
            Completable.fromAction {
                collectionSyncer.get().scheduleSyncCollections(SYNC_ALL, NON_ACTIVE_BUSINESSES_SYNCER, businessId)
            },
            Completable.fromAction {
                collectionSyncer.get().scheduleCollectionProfile(NON_ACTIVE_BUSINESSES_SYNCER, businessId)
            },
            ab.get().sync(businessId, "app_open").onErrorComplete(),
            syncContactsWithAccount.get().schedule(businessId),
            syncCustomerTxnAlert.get().schedule(businessId),
            inAppNotificationRepository.get().scheduleSyncCompletable(businessId),
            billRepository.get().scheduleBillSync(businessId),
            suggestedCustomersForAddTransactionShortcutSyncer.get().schedule(businessId),
            syncBusiness(businessId),
            syncTransactionsImpl.get().execute(NON_ACTIVE_BUSINESSES_SYNCER, businessId = businessId),
            syncCustomersImpl.get().execute(businessId),
            rxCompletable { collectionSyncer.get().executeSyncOnlinePayments(businessId) },
            supplierCreditRepository.get().syncSupplierEnabledCustomerIds(businessId = businessId),
            supplierCreditRepository.get().syncEverything(businessId),
            dueInfoSyncer.get().schedule(businessId),
            serverActionableChecker.get().schedule(businessId),
            dynamicViewRepository.get().scheduleSyncCustomizations(businessId = businessId),
        )
    }

    private fun isUserLoggedIn() = checkAuth.get().execute().firstOrError()

    private fun syncBusiness(businessId: String): Completable {
        return businessApi.get().executeSyncBusiness(businessId)
    }
}
