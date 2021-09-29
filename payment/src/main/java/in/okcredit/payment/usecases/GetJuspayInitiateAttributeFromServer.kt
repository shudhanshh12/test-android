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

class GetJuspayInitiateAttributeFromServer @Inject constructor(
    private val repository: Lazy<PaymentRepository>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>
) {
    fun execute(): Observable<Result<PaymentApiMessages.GetJuspayAttributesResponse>> {
        return UseCase.wrapSingle(
            getActiveBusiness.get().execute().firstOrError()
                .flatMap {
                    repository.get().getJuspayAttributes(
                        PaymentApiMessages.JuspayAttributeRequestBody(
                            type = JuspayEventType.INITIATE.value,
                            payerId = it.id,
                            payerEmail = it.email ?: "",
                            payerPhone = it.mobile,
                        ),
                        businessId = it.id
                    )
                }
        )
    }
}
