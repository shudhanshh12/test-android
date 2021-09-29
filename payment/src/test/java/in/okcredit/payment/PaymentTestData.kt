package `in`.okcredit.payment

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.payment.contract.model.PaymentAttributes
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import org.joda.time.DateTime

object PaymentTestData {
    val MERCHANT = Business(
        "abc",
        "abc Store",
        "8888888888",
        "",
        "",
        0.0,
        0.0,
        "",
        "",
        "",
        DateTime.now(),
        null,
        false,
        null,
        null,
        false,
        null,
        null
    )

    val PAYMENT_ATTRIBUTE = PaymentAttributes("payment_id", "polling_type")

    val PaymentAttributesResponse =
        PaymentApiMessages.GetPaymentAttributesResponse(
            paymentId = "payment_id",
            amount = "1",
            attributes = PaymentApiMessages.PaymentAttributes(
                freezePaymentPage = true,
                showPreferredMode = true,
                pollingType = "polling"
            ),
            profile = PaymentApiMessages.ProfileAttributes("", PaymentApiMessages.FeatureAttribute())
        )

    fun getPaymentDestinationResponse(type: String, address: String, name: String) =
        PaymentApiMessages.PaymentDestinationResponse(
            serviceName = "TEST",
            destinationId = "12345678",
            destination = PaymentApiMessages.DestinationResponse(
                name = name,
                type = type,
                paymentAddress = address,
                mobile = "1234567",
                upi_vpa = "upi_vpa"
            ),
            status = "STATUS",
            type = "MERCHANT"

        )
}
