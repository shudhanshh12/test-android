package `in`.okcredit.payment.server.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface PaymentApiMessages {

    @JsonClass(generateAdapter = true)
    data class GetJuspayAttributesResponse(
        @Json(name = "signature_payload")
        val signaturePayload: String,
        @Json(name = "signature")
        val signature: String,
        @Json(name = "language")
        val language: String,
        @Json(name = "end_urls")
        val endUrls: List<String>
    )

    @JsonClass(generateAdapter = true)
    data class JuspayAttributeRequestBody(
        @Json(name = "type")
        val type: String,
        @Json(name = "payment_id")
        val paymentId: String = "",
        @Json(name = "amount")
        val amount: Double = 0.0,
        @Json(name = "link_id")
        val linkId: String = "",
        @Json(name = "payer_id")
        val payerId: String = "",
        @Json(name = "payer_email")
        val payerEmail: String = "",
        @Json(name = "payer_phone")
        val payerPhone: String = "",
        @Json(name = "merchant_language")
        val merchantLanguage: Boolean = true,
    )

    @JsonClass(generateAdapter = true)
    data class PaymentAttributeRequestBody(
        @Json(name = "client")
        val client: String
    )

    @JsonClass(generateAdapter = true)
    data class GetPaymentAttributesResponse(
        @Json(name = "payment_id")
        val paymentId: String,
        @Json(name = "amount")
        val amount: String,
        @Json(name = "attributes")
        val attributes: PaymentAttributes,
        @Json(name = "profile")
        val profile: ProfileAttributes,
    )

    @JsonClass(generateAdapter = true)
    data class PaymentAttributes(
        @Json(name = "freeze_payment_page")
        val freezePaymentPage: Boolean,
        @Json(name = "show_preferred_mode")
        val showPreferredMode: Boolean,
        @Json(name = "polling_type")
        val pollingType: String
    )

    @JsonClass(generateAdapter = true)
    data class PaymentDestinationRequest(
        @Json(name = "service_name")
        val serviceName: String,
        @Json(name = "destination_id")
        val destinationId: String,
        @Json(name = "destination")
        val destination: DestinationRequest,
        @Json(name = "status")
        val status: String,
        @Json(name = "type")
        val type: String
    )

    @JsonClass(generateAdapter = true)
    data class DestinationRequest(
        @Json(name = "type")
        val type: String,
        @Json(name = "payment_address")
        val paymentAddress: String
    )

    @JsonClass(generateAdapter = true)
    data class PaymentDestinationResponse(
        @Json(name = "service_name")
        val serviceName: String,
        @Json(name = "destination_id")
        val destinationId: String,
        @Json(name = "destination")
        val destination: DestinationResponse,
        @Json(name = "status")
        val status: String,
        @Json(name = "type")
        val type: String
    )

    @JsonClass(generateAdapter = true)
    data class DestinationResponse(
        @Json(name = "name")
        val name: String,
        @Json(name = "type")
        val type: String,
        @Json(name = "payment_address")
        val paymentAddress: String,
        @Json(name = "mobile")
        val mobile: String,
        @Json(name = "upi_vpa")
        val upi_vpa: String
    )

    @JsonClass(generateAdapter = true)
    data class JuspayPaymentPollingResponse(
        @Json(name = "status")
        val status: String,
        @Json(name = "payment_id")
        val paymentId: String,
        @Json(name = "payment")
        val paymentInfo: PaymentInfo
    )

    @JsonClass(generateAdapter = true)
    data class PaymentInfo(
        @Json(name = "id")
        val id: String,
        @Json(name = "link_id")
        val linkId: String,
        @Json(name = "create_time")
        val createTime: Long,
        @Json(name = "update_time")
        val updateTime: Long,
        @Json(name = "payment_amount")
        val paymentAmount: String,
        @Json(name = "payout_amount")
        val payoutAmount: String,
        @Json(name = "refund_amount")
        val refundAmount: String
    )

    @JsonClass(generateAdapter = true)
    data class ProfileAttributes(
        @Json(name = "merchant_id")
        val merchantId: String,
        @Json(name = "features")
        val features: FeatureAttribute
    )

    @JsonClass(generateAdapter = true)
    data class FeatureAttribute(
        @Json(name = "juspay_payment_quick_pay")
        val juspayPaymentQuickPay: Boolean = false
    )
}
