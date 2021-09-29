package `in`.okcredit.cashback.repository

import `in`.okcredit.cashback.contract.model.CashbackMessageDetails
import `in`.okcredit.cashback.datasource.local.CashbackLocalCacheSource
import `in`.okcredit.cashback.datasource.remote.CashbackRemoteSource
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackEntityMapper
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import `in`.okcredit.cashback.datasource.remoteConfig.CashbackRemoteConfigSource
import `in`.okcredit.merchant.rewards.server.internal.toRewardModel
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.shared.utils.CommonUtils.currentDateTime
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CashbackRepositoryImpl @Inject constructor(
    private val cashbackRemoteSource: Lazy<CashbackRemoteSource>,
    private val cashbackLocalCacheSource: Lazy<CashbackLocalCacheSource>,
    private val cashbackRemoteConfigSource: Lazy<CashbackRemoteConfigSource>,
) : CashbackRepository {

    override fun getCashbackMessageDetails(businessId: String): Observable<CashbackMessageDetails> {
        return cashbackLocalCacheSource.get().getCachedCashbackMessageDetailsTimestamp()
            .map { timestamp ->
                val ttlInHours = cashbackRemoteConfigSource.get().getTtlForLocalCache()
                currentDateTime().millis - timestamp < TimeUnit.HOURS.toMillis(ttlInHours)
            }
            .switchMap { isCacheValid ->
                if (isCacheValid) {
                    return@switchMap cashbackLocalCacheSource.get().getCachedCashbackMessageDetails().toObservable()
                } else {
                    return@switchMap fetchFromRemoteAndStoreInLocalCache(businessId).toObservable()
                }
            }
            .map { cashbackMessageDetailsDto ->
                CashbackEntityMapper.CASHBACK_MESSAGE_DETAILS_CONVERTER.convert(cashbackMessageDetailsDto)
            }
    }

    private fun fetchFromRemoteAndStoreInLocalCache(businessId: String): Single<CashbackMessageDetailsDto> {
        return cashbackRemoteSource.get().getCashbackMessageDetails(businessId)
            .flatMap { details ->
                cashbackLocalCacheSource.get().setCashbackMessageDetailsCache(details).toSingleDefault(details)
            }
    }

    override fun getCashbackRewardForPaymentId(paymentId: String, businessId: String): Observable<RewardModel> {
        return cashbackRemoteSource.get().getCashbackRewardForPaymentId(
            paymentId,
            cashbackRemoteConfigSource.get().getCashbackRewardRequestRetryInterval(),
            cashbackRemoteConfigSource.get().getCashbackRewardRequestTimeLimit(),
            businessId
        )
            .toObservable()
            .map {
                it.toRewardModel()
            }
    }

    override fun invalidateLocalData() = cashbackLocalCacheSource.get().invalidateCache()

    override fun clearLocalData() = cashbackLocalCacheSource.get().clear()
}
