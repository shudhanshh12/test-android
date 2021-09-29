package `in`.okcredit.payment

import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import io.reactivex.Observable
import io.reactivex.Single

interface PaymentRepository {

    fun getJuspayAttributes(
        juspayAttributeRequestBody: PaymentApiMessages.JuspayAttributeRequestBody,
        businessId: String,
    ): Single<PaymentApiMessages.GetJuspayAttributesResponse>

    fun getPaymentAttributes(
        client: String,
        linkId: String,
        businessId: String,
    ): Single<PaymentApiMessages.GetPaymentAttributesResponse>

    fun getJuspayPaymentPolling(
        pid: String,
        polling: Boolean,
        type: String,
        businessId: String,
    ): Observable<PaymentModel.JuspayPaymentPollingModel>

    fun createPaymentDestination(
        paymentDestinationRequest: PaymentApiMessages.PaymentDestinationRequest,
        businessId: String,
    ): Single<PaymentApiMessages.PaymentDestinationResponse>
}
