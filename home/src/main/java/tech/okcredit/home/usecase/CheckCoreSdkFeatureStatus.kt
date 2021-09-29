package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.database.internal.CustomerDao
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CheckCoreSdkFeatureStatus @Inject constructor(
    private val ab: AbRepository,
    private val coreSdk: CoreSdk,
    private val transactionRepo: TransactionRepo,
    private val customerDao: CustomerDao,
    private val syncCustomersImpl: SyncCustomersImpl,
    private val syncTransactionsImpl: SyncTransactionsImpl,
    private val coreTracker: CoreTracker,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, Unit> {
    companion object {
        const val TAG = "CheckCoreSdkFeatureStatus"
    }

    override fun execute(req: Unit): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                checkIsCoreSdkFeatureFlagChanged(businessId)
            }
        )
    }

    private fun checkIsCoreSdkFeatureFlagChanged(businessId: String): Completable {
        return ab.isFeatureEnabled(Features.CORE_SDK, businessId = businessId)
            .firstOrError()
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable { isFeatureEnabled ->
                if (isFeatureEnabled) {
                    coreSdk.isCoreSdkFeatureEnabled(businessId)
                        .observeOn(ThreadUtils.worker())
                        .flatMapCompletable { wasAlreadyEnabled ->
                            if (wasAlreadyEnabled) {
                                Completable.complete() // No change
                            } else {
                                coreTracker.trackCoreSdkFeatureStatus("1", isFeatureEnabled, wasAlreadyEnabled)
                                transactionRepo.listDirtyTransactions(null, businessId)
                                    .firstOrError()
                                    .observeOn(ThreadUtils.worker())
                                    .map { it.isNotEmpty() }
                                    .flatMapCompletable { backendDirtyTransactionsPresent ->
                                        if (backendDirtyTransactionsPresent) {
                                            Timber.d("$TAG [6] featureFlag: $isFeatureEnabled dirtyTransactionsPresent")
                                            coreTracker.trackCoreSdkFeatureStatus(
                                                "2",
                                                isFeatureEnabled,
                                                wasAlreadyEnabled,
                                                reason = "dirtyTransactionsPresent"
                                            )
                                            Completable.complete() // Defer
                                        } else {
                                            isBackendAndCoreDatabaseTransactionLastSyncTimeSame(businessId)
                                                .observeOn(ThreadUtils.worker())
                                                .flatMapCompletable { isSame ->
                                                    if (isSame) { // Enable core sdk
                                                        Timber.d("$TAG [5] newStatus: $isFeatureEnabled")
                                                        coreTracker.trackFeatureActivated(Features.CORE_SDK)
                                                        coreTracker.trackCoreSdkFeatureStatus(
                                                            "2",
                                                            isFeatureEnabled,
                                                            wasAlreadyEnabled,
                                                            toggleSuccessful = true
                                                        )
                                                        transactionRepo.clear(businessId)
                                                            .andThen(Completable.fromAction { customerDao.deleteAllCustomers() })
                                                            .andThen(coreSdk.setCoreSdkFeatureStatus(true, businessId))
                                                            .andThen(syncCustomersImpl.schedule(businessId))
                                                            .andThen(syncTransactionsImpl.schedule("home_viewModel", businessId))
                                                    } else { // Not updated - schedule sync
                                                        Timber.d("$TAG [9] featureFlag: $isFeatureEnabled notSynced")
                                                        coreTracker.trackCoreSdkFeatureStatus(
                                                            "2",
                                                            isFeatureEnabled,
                                                            wasAlreadyEnabled,
                                                            reason = "notSynced"
                                                        )
                                                        coreSdk.scheduleSyncCustomers(businessId)
                                                            .andThen(coreSdk.scheduleSyncTransactions("home_viewModel", businessId))
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                } else {
                    coreSdk.isCoreSdkFeatureEnabled(businessId)
                        .doOnSuccess { coreTracker.trackCoreSdkFeatureStatus("0", isFeatureEnabled, it) }
                        .ignoreElement()
                }
            }
    }

    private fun isBackendAndCoreDatabaseTransactionLastSyncTimeSame(businessId: String): Single<Boolean> {
        return Single.zip(
            transactionRepo.lastUpdatedTransactionTime(businessId),
            coreSdk.lastUpdatedTransactionTime(businessId),
            BiFunction { backendLastUpdatedTime, coreLastUpdatedTime ->
                return@BiFunction backendLastUpdatedTime == coreLastUpdatedTime.seconds
            }
        )
    }
}
