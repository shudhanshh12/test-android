package `in`.okcredit.cashback.repository

import `in`.okcredit.cashback.contract.model.CashbackMessageDetails
import `in`.okcredit.rewards.contract.RewardModel
import io.reactivex.Completable
import io.reactivex.Observable

interface CashbackRepository {
    fun getCashbackMessageDetails(businessId: String): Observable<CashbackMessageDetails>

    fun getCashbackRewardForPaymentId(paymentId: String, businessId: String): Observable<RewardModel>

    fun invalidateLocalData(): Completable

    fun clearLocalData(): Completable
}
