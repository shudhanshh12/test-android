package `in`.okcredit.cashback.contract.usecase

import `in`.okcredit.rewards.contract.RewardModel
import io.reactivex.Observable

interface GetCashbackRewardForPayment {
    fun execute(paymentId: String): Observable<RewardModel>
}
