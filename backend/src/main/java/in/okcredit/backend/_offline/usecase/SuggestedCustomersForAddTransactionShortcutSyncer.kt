package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.accounting_core.contract.SuggestedCustomerIdsForAddTransaction
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.utils.AbFeatures
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.RateLimit
import tech.okcredit.android.base.workmanager.RateLimit.Companion.FRC_KEY_NON_CRITICAL_DATA_WORKER_RATE_LIMIT_HOURS
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SuggestedCustomersForAddTransactionShortcutSyncer @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val suggestedCustomerIdsForAddTransaction: Lazy<SuggestedCustomerIdsForAddTransaction>,
    private val workManager: Lazy<OkcWorkManager>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        private const val WORKER_NAME = "SuggestedCustomersForAddTransactionShortcutSyncer"
    }

    internal fun syncSuggestions(businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            syncSuggestion(_businessId)
        }
    }

    private fun syncSuggestion(businessId: String): Completable {
        return ab.get().isFeatureEnabled(AbFeatures.FEATURE_ADD_TRANSACTION_SHORTCUT, businessId = businessId)
            .firstOrError()
            .flatMapCompletable { enabled ->
                if (enabled) {
                    suggestedCustomerIdsForAddTransaction.get().getSuggestionsFromServer(businessId)
                        .flatMapCompletable { suggestedIds ->
                            suggestedCustomerIdsForAddTransaction.get()
                                .replaceSuggestedCustomerIdsForAddTransaction(suggestedIds, businessId)
                        }
                } else {
                    Completable.complete()
                }
            }.onErrorComplete()
    }

    fun schedule(businessId: String): Completable {
        return Single.fromCallable {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return@fromCallable OneTimeWorkRequest.Builder(SuggestedCustomersForAddTransactionShortcutSyncWorker::class.java)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        SuggestedCustomersForAddTransactionShortcutSyncWorker.BUSINESS_ID to businessId
                    )
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()
        }.flatMapCompletable { workRequest ->
            val hours = firebaseRemoteConfig.get().getLong(FRC_KEY_NON_CRITICAL_DATA_WORKER_RATE_LIMIT_HOURS)

            val rateLimit = RateLimit(hours, TimeUnit.HOURS)

            workManager.get().scheduleWithRateLimitRx(
                WORKER_NAME,
                Scope.Business(businessId),
                ExistingWorkPolicy.KEEP,
                workRequest,
                rateLimit,
            )
        }.subscribeOn(ThreadUtils.newThread())
    }
}

class SuggestedCustomersForAddTransactionShortcutSyncWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val suggestedCustomersForAddTransactionShortcutSyncer: SuggestedCustomersForAddTransactionShortcutSyncer,
) : BaseRxWorker(context, params) {
    companion object {
        const val BUSINESS_ID = "business-id"
    }

    override fun doRxWork(): Completable {
        val businessId = inputData.getString(BUSINESS_ID)
        return suggestedCustomersForAddTransactionShortcutSyncer.syncSuggestions(businessId)
    }

    class Factory @Inject constructor(
        private val suggestedCustomersForAddTransactionShortcutSyncer: Lazy<SuggestedCustomersForAddTransactionShortcutSyncer>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return SuggestedCustomersForAddTransactionShortcutSyncWorker(
                context,
                params,
                suggestedCustomersForAddTransactionShortcutSyncer.get()
            )
        }
    }
}
