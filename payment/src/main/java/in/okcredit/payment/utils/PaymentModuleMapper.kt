package `in`.okcredit.payment.utils

import `in`.okcredit.payment.contract.model.PaymentInfo
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.server.internal.PaymentApiMessages

object PaymentModuleMapper {

    fun toPaymentResponseModel(response: PaymentApiMessages.JuspayPaymentPollingResponse) =
        PaymentModel.JuspayPaymentPollingModel(
            status = response.status,
            paymentId = response.paymentId,
            paymentInfo = PaymentInfo(
                response.paymentInfo.id,
                response.paymentInfo.linkId,
                response.paymentInfo.createTime,
                response.paymentInfo.updateTime,
                response.paymentInfo.paymentAmount,
                response.paymentInfo.payoutAmount,
                response.paymentInfo.refundAmount
            )
        )
}
