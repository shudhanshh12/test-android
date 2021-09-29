package `in`.okcredit.cashback.datasource.remote

import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackApiClient
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages
import dagger.Lazy
import io.reactivex.Flowable
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CashbackRemoteSourceImpl @Inject constructor(
    private val cashbackApiClient: Lazy<CashbackApiClient>,
) : CashbackRemoteSource {

    override fun getCashbackMessageDetails(businessId: String): Single<CashbackMessageDetailsDto> {

        return cashbackApiClient.get().getCashbackMessageDetails(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    return@map response.body()
                } else {
                    throw response.asError()
                }
            }
    }

    override fun getCashbackRewardForPaymentId(
        paymentId: String,
        requestRetryInterval: Long,
        timeLimit: Long,
        businessId: String,
    ): Flowable<ApiMessages.RewardFromApi> {

        var timeElapsed = 0L

        return cashbackApiClient.get().getCashbackRewardForPaymentId(paymentId, businessId)
            .repeatWhen {
                it.delay(requestRetryInterval, TimeUnit.MILLISECONDS)
            }
            .takeUntil {
                timeElapsed += requestRetryInterval
                it.body() != null && it.isSuccessful || timeElapsed > timeLimit
            }
            .filter {
                // when totalTime > maxLimit is true for takeuntil call,  we need to pass last pending response to usecase
                // so totalTime > maxLimit.minus(delayTime) wil use
                it.body() != null && it.isSuccessful || timeElapsed > timeLimit.minus(requestRetryInterval)
            }
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    return@map response.body()
                } else {
                    throw response.asError()
                }
            }
    }
}
