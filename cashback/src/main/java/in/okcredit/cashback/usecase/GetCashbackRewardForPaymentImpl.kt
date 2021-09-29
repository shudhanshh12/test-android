package `in`.okcredit.cashback.usecase

import `in`.okcredit.cashback.contract.usecase.GetCashbackRewardForPayment
import `in`.okcredit.cashback.repository.CashbackRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.rewards.contract.RewardModel
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCashbackRewardForPaymentImpl @Inject constructor(
    private val cashbackRepository: Lazy<CashbackRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetCashbackRewardForPayment {

    override fun execute(paymentId: String): Observable<RewardModel> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            cashbackRepository.get().getCashbackRewardForPaymentId(paymentId, businessId)
        }
    }
}
