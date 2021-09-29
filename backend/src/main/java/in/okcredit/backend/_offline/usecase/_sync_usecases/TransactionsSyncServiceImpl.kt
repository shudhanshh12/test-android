package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class TransactionsSyncServiceImpl @Inject constructor(
    private val rxSharedPreference: Lazy<DefaultPreferences>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) :
    TransactionsSyncService {
    override fun isSyncedAtLeastOnce(): Single<Boolean> =
        getActiveBusinessId.get().execute().flatMap { businessId ->
            rxSharedPreference.get()
                .getLong(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, Scope.Business(businessId))
                .asObservable().firstOrError()
                .map { it > 0L }
        }

    override fun getLastSyncTime(businessId: String): Single<Long> = rxSharedPreference.get()
        .getLong(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, Scope.Business(businessId))
        .asObservable().firstOrError()

    override fun setLastSyncTime(time: DateTime, businessId: String): Completable {
        return rxCompletable {
            rxSharedPreference.get()
                .set(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, time.millis, Scope.Business(businessId))
        }
    }

    override fun clearLastSyncTime(): Completable {
        return rxCompletable {
            getBusinessIdList.get().execute().first().forEach { businessId ->
                rxSharedPreference.get().set(PREF_BUSINESS_LAST_TRANSACTION_SYNC_TIME, -1L, Scope.Business(businessId))
            }
        }
    }
}
