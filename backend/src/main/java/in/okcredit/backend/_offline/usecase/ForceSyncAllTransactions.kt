package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.shared.service.keyval.KeyValService
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class ForceSyncAllTransactions @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val keyValService: Lazy<KeyValService>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) {

    companion object {
        // TODO: Should Remove this feature flag and IS_FORCE_SYNC_ONCE once we move to server action
        const val FEATURE_FORCE_SYNC = "force_sync_transactions"
    }

    fun executeWithFeatureFlagCheck(): Completable {
        return ab.get().isFeatureEnabled(FEATURE_FORCE_SYNC).firstOrError()
            .flatMapCompletable { isForceSyncFeatureEnabled ->
                isForceSyncRunsOnce().firstOrError().flatMapCompletable { isForceSyncOnce ->
                    if (isForceSyncFeatureEnabled && isForceSyncOnce.not()) {
                        return@flatMapCompletable rxCompletable {
                            getBusinessIdList.get().execute().first().forEach { businessId ->
                                syncTransactionsImpl.get().executeForceSync(businessId = businessId)
                            }
                        }.andThen(
                            keyValService.get()
                                .put(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE, "true", Scope.Individual)
                        )
                    } else {
                        return@flatMapCompletable Completable.complete()
                    }
                }
            }
    }

    fun executeForceSync(): Completable {
        return syncTransactionsImpl.get().executeForceSync()
    }

    private fun isForceSyncRunsOnce(): Observable<Boolean> {
        return keyValService.get().contains(PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE, Scope.Individual)
            .flatMapObservable { it ->
                if (it) {
                    keyValService.get()[PREF_INDIVIDUAL_IS_FORCE_SYNC_ONCE, Scope.Individual].flatMap {
                        return@flatMap Observable.just(it.isNotEmpty() && it == "true")
                    }
                } else {
                    return@flatMapObservable Observable.just(false)
                }
            }
    }
}
