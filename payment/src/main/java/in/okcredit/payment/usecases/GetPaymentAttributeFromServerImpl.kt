package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.contract.model.PaymentAttributes
import `in`.okcredit.payment.contract.usecase.GetPaymentAttributeFromServer
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentAttributeFromServerImpl @Inject constructor(
    private val repository: Lazy<PaymentRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    GetPaymentAttributeFromServer {

    override fun execute(
        client: String,
        linkId: String,
    ): Single<PaymentAttributes> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            repository.get().getPaymentAttributes(client = client, linkId = linkId, businessId = businessId).map {
                PaymentAttributes(
                    paymentId = it.paymentId,
                    pollingType = it.attributes.pollingType,
                    quickPayEnabled = it.profile.features.juspayPaymentQuickPay
                )
            }
        }
    }
}
