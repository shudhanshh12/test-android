package `in`.okcredit.payment.contract.usecase

import `in`.okcredit.payment.contract.model.PaymentModel
import io.reactivex.Observable

interface GetPaymentResult {

    fun execute(
        pId: String,
        polling: Boolean,
        type: String
    ): Observable<PaymentModel.JuspayPaymentPollingModel>
}
