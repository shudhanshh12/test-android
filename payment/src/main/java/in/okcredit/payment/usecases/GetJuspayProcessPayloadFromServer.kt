package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.server.internal.JuspayEventType
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetJuspayProcessPayloadFromServer @Inject constructor(
    private val repository: Lazy<PaymentRepository>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>
) {
    fun execute(
        paymentId: String,
        amount: Double,
        linkId: String
    ): Observable<Result<PaymentApiMessages.GetJuspayAttributesResponse>> {
        return UseCase.wrapSingle(
            getActiveBusiness.get().execute().firstOrError()
                .flatMap { business ->
                    repository.get().getJuspayAttributes(
                        PaymentApiMessages.JuspayAttributeRequestBody(
                            type = JuspayEventType.PROCESS.value,
                            paymentId = paymentId,
                            amount = amount,
                            linkId = linkId,
                            payerId = business.id,
                            payerPhone = business.mobile,
                            payerEmail = if (business.email != null) business.email!! else ""
                        ),
                        businessId = business.id
                    )
                }
        )
    }
}
