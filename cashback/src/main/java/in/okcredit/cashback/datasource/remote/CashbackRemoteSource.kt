package `in`.okcredit.cashback.datasource.remote

import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages
import io.reactivex.Flowable
import io.reactivex.Single

interface CashbackRemoteSource {
    fun getCashbackMessageDetails(businessId: String): Single<CashbackMessageDetailsDto>

    fun getCashbackRewardForPaymentId(
        paymentId: String,
        requestRetryInterval: Long,
        timeLimit: Long,
        businessId: String
    ): Flowable<ApiMessages.RewardFromApi>
}
