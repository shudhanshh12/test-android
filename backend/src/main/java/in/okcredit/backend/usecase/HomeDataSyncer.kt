package `in`.okcredit.backend.usecase

import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.ServerActionableChecker
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.crashlytics.RecordException
import timber.log.Timber
import javax.inject.Inject

/**
 * [HomeDataSyncer] includes data sync calls for :
 * - data required on the home screen
 * - critical data (accounting)
 *
 * Trigger : home screen open
 * Rate limit : once in 2 hours
 */
class HomeDataSyncer @Inject constructor(
    private val checkAuth: Lazy<CheckAuth>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val syncCustomersImpl: Lazy<SyncCustomersImpl>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val dueInfoSyncer: Lazy<DueInfoSyncer>,
    private val serverActionableChecker: Lazy<ServerActionableChecker>,
    private val dynamicViewRepository: Lazy<DynamicViewRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val syncIndividual: Lazy<SyncIndividual>,
    private val businessRepository: Lazy<BusinessRepository>,
) {
    companion object {
        @NonNls
        const val SOURCE = "sync_app_start"
    }

    fun execute(): Completable {
        return isUserLoggedIn()
            .flatMapCompletable { isUserLoggedIn ->
                if (isUserLoggedIn) {
                    getActiveBusinessId.get().execute().flatMapCompletable { _businessId ->
                        Completable.mergeArray(
                            rxCompletable { syncIndividual.get().syncIndividualAndNewBusinessesIfPresent() },
                            syncBusiness(_businessId),
                            syncTransactionsImpl.get().execute(SOURCE, businessId = _businessId),
                            syncCustomersImpl.get().execute(_businessId),
                            rxCompletable { collectionSyncer.get().executeSyncOnlinePayments(_businessId) },
                            rxCompletable { collectionSyncer.get().executeSyncCustomerCollections(_businessId) },
                            supplierCreditRepository.get().syncSupplierEnabledCustomerIds(businessId = _businessId),
                            supplierCreditRepository.get().syncEverything(_businessId),
                            dueInfoSyncer.get().schedule(_businessId),
                            serverActionableChecker.get().schedule(_businessId),
                            dynamicViewRepository.get().scheduleSyncCustomizations(businessId = _businessId),
                        )
                    }
                } else {
                    Completable.complete()
                }
            }.doOnComplete {
                Timber.d("<<<HomeDataSyncer completed")
            }.doOnError {
                RecordException.recordException(it)
                Timber.e("<<<<Worker Error HomeDataSyncer error ${it.message}")
            }
    }

    private fun isUserLoggedIn() = checkAuth.get().execute().firstOrError()

    private fun syncBusiness(businessId: String): Completable {
        return businessRepository.get().executeSyncBusiness(businessId)
    }
}
