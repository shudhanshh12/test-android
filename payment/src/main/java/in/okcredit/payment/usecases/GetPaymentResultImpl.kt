package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.contract.usecase.GetPaymentResult
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetPaymentResultImpl @Inject constructor(
    private val repository: Lazy<PaymentRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetPaymentResult {

    override fun execute(
        pId: String,
        polling: Boolean,
        type: String,
    ): Observable<PaymentModel.JuspayPaymentPollingModel> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            repository.get().getJuspayPaymentPolling(pid = pId, polling = polling, type = type, businessId = businessId)
        }
    }
}
