package `in`.okcredit.frontend.usecase

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.DueInfoSyncer
import `in`.okcredit.backend._offline.usecase.LinkDevice
import `in`.okcredit.backend._offline.usecase.SetMerchantPreference
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.Features
import `in`.okcredit.backend.contract.PeriodicDataSyncWorker
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.dynamicview.data.repository.DynamicViewRepositoryImpl
import `in`.okcredit.frontend.contract.LoginDataSyncer
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.SetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import com.google.firebase.perf.FirebasePerformance
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import merchant.android.okstream.contract.OkStreamService
import merchant.okcredit.user_stories.contract.UserStoryRepository
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.ab.Profile
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.bills.BillRepository
import tech.okcredit.userSupport.SupportRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * LoginDataSyncer is responsible for syncing (downloading) user's data right after login.
 * Only critical data (required for core functionality) should be synced in this class.
 *
 * Non-critical data sync calls for business and individual should be added
 * to [syncMiscellaneousDataForBusiness] and [syncMiscellaneousDataForIndividual] methods respectively.
 * Failure of non-critical data sync should not fail the whole usecase (add onErrorComplete or equivalent)
 */
class LoginDataSyncerImpl @Inject constructor(
    private val businessRepository: Lazy<BusinessRepository>,
    private val syncCustomersImpl: Lazy<SyncCustomersImpl>,
    private val ab: Lazy<AbRepository>,
    private val tracker: Lazy<Tracker>,
    private val keyValService: Lazy<KeyValService>,
    private val setMerchantPreference: Lazy<SetMerchantPreference>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val linkDevice: Lazy<LinkDevice>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val rewardsSyncer: Lazy<RewardsSyncer>,
    private val referralRepository: Lazy<ReferralRepository>,
    private val dueInfoSyncer: Lazy<DueInfoSyncer>,
    private val dynamicViewRepository: Lazy<DynamicViewRepositoryImpl>,
    private val coreSdk: Lazy<CoreSdk>,
    private val authService: Lazy<AuthService>,
    private val billRepository: Lazy<BillRepository>,
    firebasePerformance: Lazy<FirebasePerformance>,
    private val localeManager: Lazy<LocaleManager>,
    private val userStoryRepository: Lazy<UserStoryRepository>,
    private val okStreamService: Lazy<OkStreamService>,
    private val context: Lazy<Context>,
    private val userSupport: Lazy<SupportRepository>,
    private val periodicDataSyncWorker: Lazy<PeriodicDataSyncWorker>,
    private val syncIndividual: Lazy<SyncIndividual>,
    private val setActiveBusinessId: Lazy<SetActiveBusinessId>,
) : UseCase<BehaviorSubject<SyncState>, Unit>, LoginDataSyncer {

    companion object {
        const val TAG = "<<<<LoginDataSyncer"
    }

    private val traceSync = firebasePerformance.get().newTrace("syncAuthPerformance")

    override fun execute(req: BehaviorSubject<SyncState>): Observable<Result<Unit>> {
        val timeStartInSec = System.currentTimeMillis()
        traceSync.start()

        return UseCase.wrapCompletable(
            rxCompletable {
                val businessIdList = syncIndividual.get().execute().businessIdList
                setActiveBusinessId(businessIdList.first())
                businessIdList.forEach { businessId ->
                    fetchBusiness(businessId)
                        .andThen(syncDataForBusinessId(req, timeStartInSec, businessId))
                        .await()
                }
                syncMiscellaneousDataForIndividual(businessIdList.first()).await()
                req.onNext(SyncState.COMPLETED)
            }
        )
    }

    private suspend fun setActiveBusinessId(businessId: String) {
        setActiveBusinessId.get().execute(businessId).await()
    }

    private fun fetchBusiness(businessId: String): Completable {
        return businessRepository.get().executeSyncBusiness(businessId).doOnError {
            RecordException.recordException(it)
            tracker.get().trackError("SyncScreen", "MerchantApiError", it.message ?: "", "")
        }
    }

    override fun syncDataForBusinessId(
        req: BehaviorSubject<SyncState>,
        timeStartInSec: Long?,
        businessId: String,
    ): Completable {
        return syncAbProfile(businessId)
            .doOnComplete {
                timeStartInSec?.let {
                    traceSync.incrementMetric("step_count", 1)
                    traceSync.putMetric("SyncAbProfileTime", System.currentTimeMillis().minus(timeStartInSec))
                    Timber.v("$TAG AB Profile Sync Completed")
                }
            }
            .andThen(setCoreSdkFeatureStatus(businessId))
            .andThen(syncMiscellaneousDataForBusiness(businessId)).doOnComplete {
                timeStartInSec?.let {
                    traceSync.incrementMetric("step_count", 1)
                    traceSync.putMetric("SyncOthersTime", System.currentTimeMillis().minus(timeStartInSec))
                    Timber.v("$TAG Schedule background tasks Completed")
                }
            }
            .andThen(syncAccountingData(req, businessId))
            .doOnComplete { // Customers, Suppliers, Customer Txns, Supplier Txns
                timeStartInSec?.let {
                    traceSync.incrementMetric("step_count", 1)
                    traceSync.putMetric("syncAccountingData", System.currentTimeMillis().minus(timeStartInSec))
                    traceSync.stop()
                    Timber.v("$TAG Accounting Sync Completed")
                }
            }
    }

    private fun setCoreSdkFeatureStatus(businessId: String): Completable {
        return ab.get().isFeatureEnabled(Features.CORE_SDK, true, businessId).firstOrError()
            .flatMapCompletable { coreSdk.get().setCoreSdkFeatureStatus(it, businessId) }
    }

    private fun fetchCustomers(businessId: String): Completable {
        return syncCustomersImpl.get().execute(businessId).doOnError {
            RecordException.recordException(it)
            tracker.get().trackError("SyncScreen", "CustomerApiError", it.message ?: "", "")
        }
    }

    private fun syncDynamicComponentData(businessId: String): Completable {
        return rxCompletable { dynamicViewRepository.get().syncCustomizations(businessId) }
            .onErrorResumeNext { dynamicViewRepository.get().scheduleSyncCustomizations(businessId) }
    }

    private fun syncReferral(businessId: String): Completable {
        return referralRepository.get().checkQualificationJourney(businessId).onErrorComplete()
    }

    private fun syncAbProfile(businessId: String): Completable {
        traceSync.putAttribute("merchant_id", businessId)
        return ab.get().sync(businessId, "login").onErrorResumeNext {
            // We'll by default enable CoreSdk feature for all users who are logging in
            ab.get().setProfile(Profile(features = mapOf(Features.CORE_SDK to true)), businessId)
        }
    }

    private fun syncAccountingData(req: BehaviorSubject<SyncState>, businessId: String): Completable {
        return Completable.mergeArray(
            fetchCustomers(businessId),
            syncTransactionsImpl.get().execute("sync_screen", req, true, businessId = businessId),
            keyValService.get().put(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE, "true", Scope.Individual),
            supplierCreditRepository.get().syncAllTransactions(businessId = businessId),
            supplierCreditRepository.get().syncSuppliers(businessId)
        )
    }

    private fun syncMiscellaneousDataForBusiness(businessId: String): Completable {
        return Completable.mergeArray(
            syncMerchantLang(),
            Completable.fromAction {
                collectionSyncer.get().scheduleSyncEverything(CollectionSyncer.Source.SYNC_SCREEN, businessId)
            },
            syncDynamicComponentData(businessId).onErrorComplete(),
            businessRepository.get().scheduleSyncBusinessCategoriesAndBusinessTypes(businessId).onErrorComplete(),
            dueInfoSyncer.get().schedule(businessId).onErrorComplete(),
            billRepository.get().scheduleBillSync(businessId).onErrorComplete(),
            billRepository.get().resetBillAdoptionTime(businessId).onErrorComplete(),
            supplierCreditRepository.get().syncSupplierEnabledCustomerIds(businessId).onErrorComplete(),
            syncUserStories(businessId).onErrorComplete(),
            schedulePeriodicDataSyncWorker(businessId).onErrorComplete(),
        )
    }

    private fun syncMiscellaneousDataForIndividual(businessId: String): Completable {
        return Completable.mergeArray(
            connectToOkStreamService().onErrorComplete(),
            scheduleSyncHelpData(businessId).onErrorComplete(),
            linkDevice.get().schedule().onErrorComplete(),
            rewardsSyncer.get().scheduleEverything(businessId).onErrorComplete(),
            syncReferral(businessId),
            syncPassword(),
        )
    }

    private fun syncPassword(): Completable {
        return Completable.fromAction { authService.get().syncPassword() }
            .onErrorComplete()
            .subscribeOn(ThreadUtils.api())
    }

    private fun syncMerchantLang(): Completable {
        return setMerchantPreference.get()
            .execute(PreferenceKey.LANGUAGE, localeManager.get().getLanguage())
    }

    private fun syncUserStories(businessId: String): Completable {
        return Completable.mergeArray(
            userStoryRepository.get().syncMyStory(businessId).onErrorComplete(),
            userStoryRepository.get().syncOtherStory(businessId).onErrorComplete()
        )
    }

    private fun scheduleSyncHelpData(businessId: String): Completable {
        return userSupport.get().scheduleSyncEverything(localeManager.get().getLanguage(), businessId)
    }

    private fun connectToOkStreamService() = Completable.fromAction { okStreamService.get().connect(context.get()) }

    private fun schedulePeriodicDataSyncWorker(businessId: String) =
        Completable.fromAction { periodicDataSyncWorker.get().schedule(businessId) }
}
