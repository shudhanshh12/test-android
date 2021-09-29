package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class SetPaymentDestinationToServer @Inject constructor(
    private val repository: Lazy<PaymentRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(
        adoptionSource: String,
        paymentAddress: String,
        paymentType: String
    ): Observable<Result<PaymentApiMessages.PaymentDestinationResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute()
                .flatMap {
                    repository.get().createPaymentDestination(
                        PaymentApiMessages.PaymentDestinationRequest(
                            serviceName = adoptionSource,
                            destination = PaymentApiMessages.DestinationRequest(
                                type = paymentType,
                                paymentAddress = paymentAddress
                            ),
                            destinationId = it,
                            status = "ACTIVE",
                            type = "MERCHANT"
                        ),
                        businessId = it
                    )
                }
        )
    }
}
