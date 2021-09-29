package `in`.okcredit.payment.contract.usecase

import `in`.okcredit.payment.contract.model.PaymentAttributes
import io.reactivex.Single

interface GetPaymentAttributeFromServer {
    fun execute(client: String, linkId: String): Single<PaymentAttributes>
}
