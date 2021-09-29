package `in`.okcredit.collection.contract

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

interface ApiMessages {

    data class Collection(
        @SerializedName("id")
        val id: String,

        @SerializedName("create_time")
        val create_time: DateTime,

        @SerializedName("update_time")
        val update_time: DateTime,

        @SerializedName("labels")
        val labels: Map<String, String>?,

        @SerializedName("status")
        val status: Int,

        @SerializedName("payment_link")
        val payment_link: String,

        @SerializedName("amount_requested")
        val amount_requested: Long,

        @SerializedName("amount_collected")
        val amount_collected: Long?,

        @SerializedName("fee")
        val fee: Long?,

        @SerializedName("expire_time")
        val expire_time: DateTime?,

        @SerializedName("customer_id")
        val customer_id: String,

        @SerializedName("discount")
        val discount: Long?,

        @SerializedName("fee_category")
        val fee_category: Int,

        @SerializedName("settlement_category")
        val settlement_category: Int,

        @SerializedName("merchantName")
        val merchantName: String?,

        @SerializedName("events")
        val events: List<Event>,

        @SerializedName("payment")
        val payment: Payment,

        @SerializedName("payout")
        val payout: Payout,

        @SerializedName("payment_id")
        val paymentId: String,

        @SerializedName("error_code")
        val errorCode: String?,

        @SerializedName("error_description")
        val errorDescription: String?,

        @SerializedName("cashback_given")
        val cashbackGiven: Boolean?,
    )

    data class Event(
        @SerializedName("status")
        val status: Int,

        @SerializedName("timestamp")
        val timestamp: DateTime,
    )

    data class Payment(
        @SerializedName("provider")
        val provider: String,

        @SerializedName("id")
        val id: String,

        @SerializedName("UTR")
        val UTR: String?,

        @SerializedName("reference")
        val reference: String?,

        @SerializedName("status")
        val status: Int,

        @SerializedName("amount")
        val amount: Long?,

        @SerializedName("few")
        val fee: Long?,

        @SerializedName("tax")
        val tax: Long?,

        @SerializedName("payment_link")
        val payment_link: String,

        @SerializedName("description")
        val description: String?,

        @SerializedName("request_type")
        val request_type: Int,

        @SerializedName("labels")
        val labels: Map<String, String>,

        @SerializedName("origin")
        val origin: Origin?,
    )

    data class Origin(
        @SerializedName("mobile")
        val mobile: String,

        @SerializedName("name")
        val name: String?,

        @SerializedName("payment_address")
        val payment_address: String,

        @SerializedName("type")
        val type: String,
    )

    data class Payout(
        @SerializedName("provider")
        val provider: String,

        @SerializedName("id")
        val id: String,

        @SerializedName("UTR")
        val UTR: String,

        @SerializedName("reference")
        val reference: String,

        @SerializedName("status")
        val status: Int,

        @SerializedName("amount")
        val amount: Long,

        @SerializedName("destination")
        val destination: Destination,

        @SerializedName("fee")
        val fee: Long?,

        @SerializedName("labels")
        val labels: Map<String, String>,

        @SerializedName("tax")
        val tax: Long?,
    )

    data class Destination(
        @SerializedName("mobile")
        val mobile: String? = "",

        @SerializedName("name")
        val name: String? = "",

        @SerializedName("payment_address")
        val paymentAddress: String,

        @SerializedName("type")
        val type: String,

        @SerializedName("upi_vpa")
        val upiVpa: String? = "",
    )

    data class MerchantCollectionProfileResponse(
        @SerializedName("customers")
        val customers: List<CustomerCollectionProfileResponse>?,

        @SerializedName("suppliers")
        val suppliers: List<SupplierCollectionProfileResponse>?,

        @SerializedName("destination")
        val destination: Destination?,

        @SerializedName("merchant_id")
        val merchantId: String?, // this could be null in case when destination is not present and we receive error from server

        @SerializedName("merchant_vpa")
        val merchantVpa: String?,

        @SerializedName("limit_type")
        val limitType: String?,

        @SerializedName("limit")
        val limit: Long?,

        @SerializedName("merchant_qr_enabled")
        val merchantQrEnabled: Boolean?,

        @SerializedName("remaining_limit")
        val remainingLimit: Long?,

        @SerializedName("eta")
        val eta: Long?,
    )

    data class CustomerCollectionProfileResponse(
        @SerializedName("customer_id")
        val customer_id: String?, // this could be null in case when profile is not present and we receive error from server

        @SerializedName("profile")
        val profile: Profile?,

        @SerializedName("destination")
        val destination: Destination?,

        @SerializedName("gpay_enabled")
        val gpay_enabled: Boolean?,

        @SerializedName("cashback_eligible")
        val cashbackEligible: Boolean?,
    )

    data class SupplierCollectionProfileResponse(
        @SerializedName("account_id")
        val accountId: String?, // this could be null in case when profile/destination is not present and we receive error from server

        @SerializedName("profile")
        val supplierProfile: SupplierProfile?,

        @SerializedName("destination")
        val destination: SupplierDestination?,

        @SerializedName("destination_update_allowed")
        val destinationUpdateAllowed: Boolean?,
    )

    data class SupplierProfile(
        @SerializedName("message_link")
        val messageLink: String?,

        @SerializedName("link_intent")
        val linkIntent: String?,

        @SerializedName("linkVpa")
        val linkVpa: String?,

        @SerializedName("link_id")
        val linkId: String?,
    )

    data class SupplierDestination(
        @SerializedName("mobile")
        val mobile: String? = "",

        @SerializedName("name")
        val name: String? = "",

        @SerializedName("payment_address")
        val paymentAddress: String?,

        @SerializedName("type")
        val type: String?,

        @SerializedName("upi_vpa")
        val upiVpa: String? = "",
    )

    data class MerchantCollectionProfileRequest(
        @SerializedName("merchant_id")
        val merchant_id: String,
    )

    data class CustomerCollectionProfileRequest(
        @SerializedName("merchant_id")
        val merchant_id: String,

        @SerializedName("customer_id")
        val customer_id: String,
    )

    data class GetSigleListCustomerCollectionProfileRequest(

        @SerializedName("account_id")
        val account_id: String,
    )

    data class Profile(
        @SerializedName("customer_id")
        val customer_id: String,

        @SerializedName("message_link")
        val message_link: String?,

        @SerializedName("message")
        val message: String?,

        @SerializedName("link_intent")
        val link_intent: String?,

        @SerializedName("qr_intent")
        val qr_intent: String?,

        @SerializedName("show_image")
        val show_image: Boolean,

        @SerializedName("from_merchant_payment_link")
        val from_merchant_payment_link: String?,

        @SerializedName("from_merchant_upi_intent")
        val from_merchant_upi_intent: String?,

        @SerializedName("link_id")
        val linkId: String?,

        @SerializedName("payment_intent")
        val paymentIntent: Boolean,
    )

    data class BatchCreateCollectionsRequest(
        @SerializedName("merchant_id")
        val merchant_id: String,

        @SerializedName("requests")
        val requests: List<Request>,
    )

    data class Request(
        @SerializedName("customer_id")
        val customer_id: String,
    )

    data class SetActiveDestinationRequest(
        @SerializedName("merchant_id")
        val merchant_id: String,

        @SerializedName("destination")
        val destination: DestinationRequest,

        @SerializedName("async")
        val async: Boolean = false,

        @SerializedName("referral_merchant")
        val referralMerchant: String = "",
    )

    data class ValidatePaymentAddressRequest(
        @SerializedName("payment_address_type")
        val payment_address_type: String,

        @SerializedName("payment_address")
        val payment_address: String,
    )

    data class GetSupplierCollectionProfileRequest(
        @SerializedName("account_id")
        val accountId: String,
    )

    data class ValidatePaymentAddressResponse(
        @SerializedName("valid")
        val valid: Boolean,

        @SerializedName("name")
        val name: String?,
    )

    data class PredictedMerchantCollectionProfileResponse(
        @SerializedName("success")
        val success: Boolean,

        @SerializedName("merchant_id")
        val merchant_id: String,

        @SerializedName("destination")
        val destination: Destination,
    )

    data class EnableCustomerPayment(
        @SerializedName("campaign")
        val campaign: String,
    )

    data class GetKycExternalResponse(
        @SerializedName("kyc")
        val kyc: String,
        @SerializedName("upi_daily_limit")
        val upiDailyLimit: Long,
        @SerializedName("non_upi_daily_limit")
        val nonUpiDailyLimit: Long,
        @SerializedName("upi_daily_transaction_amount")
        val upiDailyTransactionAmount: Long,
        @SerializedName("non_upi_daily_transaction_amount")
        val nonUpiDailyTransactionAmount: Long,
    )

    data class GetKycRiskCategoryResponse(
        @SerializedName("category")
        val category: String,
    )

    data class GetRiskAttributesRequest(
        @SerializedName("user_id")
        val merchantId: String,
        @SerializedName("user_type")
        val userType: String = "MERCHANT",
        @SerializedName("service_name")
        val serviceName: String = "collection",
    )

    data class GetRiskAttributesResponse(
        @SerializedName("kyc_info")
        val kycInfo: KycInfo,
        @SerializedName("risk_category")
        val riskCategory: String,
        @SerializedName("limit_info")
        val limitInfo: LimitInfo,
    )

    data class LimitInfo(
        @SerializedName("upi_limit_info")
        val upiLimit: Limit,
        @SerializedName("non_upi_limit_info")
        val nonUpiLimit: Limit,
    )

    data class Limit(
        @SerializedName("daily_limit_reached")
        val dailyLimitReached: Boolean,
        @SerializedName("total_daily_amount_limit")
        val totalDailyAmountLimit: Long,
        @SerializedName("total_daily_limit_used")
        val totalDailyLimitUsed: Long,
        @SerializedName("total_daily_transaction_limit")
        val totalDailyTransactionLimit: Long,
        @SerializedName("remaining_daily_amount_limit")
        val remainingDailyAmountLimit: Long,
        @SerializedName("remaining_daily_transaction_limit")
        val remainingDailyTransactionLimit: Long,
    )

    data class KycInfo(
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("user_type")
        val userType: String,
        @SerializedName("pan_verified")
        val panVerified: Boolean,
        @SerializedName("aadhaar_verified")
        val aadhaarVerified: Boolean,
        @SerializedName("service_name")
        val serviceName: String,
        @SerializedName("kyc_status")
        val kycStatus: String,
        @SerializedName("address")
        val address: String,
        @SerializedName("address_type")
        val addressType: String,
        @SerializedName("created_at")
        val createdAt: DateTime,
        @SerializedName("updated_at")
        val updatedAt: DateTime,
    )

    data class GetKycRiskCategoryRequest(
        @SerializedName("merchant_id")
        val merchantId: String,
    )

    data class GetKycExternalRequest(
        @SerializedName("merchant_id")
        val merchantId: String,
    )

    data class GetOnlinePaymentsRequest(
        @SerializedName("update_time")
        val updateTime: Long? = null,
    )

    data class TagMerchantPaymentRequest(
        @SerializedName("account_id")
        val accountId: String,
        @SerializedName("collection_id")
        val paymentId: String,
    )

    data class CollectionOnlinePaymentApi(
        @SerializedName("id")
        val id: String,
        @SerializedName("create_time")
        val createdTime: DateTime,
        @SerializedName("update_time")
        val updatedTime: DateTime,
        @SerializedName("status")
        val status: Int,
        @SerializedName("merchant_id")
        val merchantId: String,
        @SerializedName("account_id")
        val accountId: String? = "",
        @SerializedName("amount")
        val amount: Double,
        @SerializedName("payment_id")
        val paymentId: String? = "",
        @SerializedName("payout_id")
        val payoutId: String? = "",
        @SerializedName("payment_source")
        val paymentSource: String? = "",
        @SerializedName("payment_mode")
        val paymentMode: String? = "",
        @SerializedName("type")
        val type: String,
        @SerializedName("error_code")
        val errorCode: String? = "",
        @SerializedName("error_description")
        val errorDescription: String? = "",
        @SerializedName("tags")
        val tags: TagResponse? = null,
    )

    data class GetOnlinePaymentResponse(
        @SerializedName("merchant_collections")
        val onlinePayments: List<CollectionOnlinePaymentApi>,
    )

    data class SetPaymentOutDestinationRequest(
        @SerializedName("account_id")
        val account_id: String,

        @SerializedName("destination")
        val destination: PaymentOutDestination,

        @SerializedName("account_type")
        val accountType: String,
    )

    data class PaymentOutDestination(
        @SerializedName("type")
        val type: String,

        @SerializedName("payment_address")
        val paymentAddress: String,
    )

    data class SetPaymentOutDestinationResponse(
        @SerializedName("success")
        val success: Boolean,
    )

    data class GetPaymentOutLinkDetailRequest(
        @SerializedName("account_id")
        val account_id: String,

        @SerializedName("account_type")
        val accountType: String,
    )

    data class PaymentOutLinkDetailResponse(
        @SerializedName("payment_out_link")
        val paymentOutLink: PaymentOutLink?,
    )

    data class PaymentOutLink(
        @SerializedName("account_id")
        val account_id: String,
        @SerializedName("account_type")
        val account_type: String,
        @SerializedName("profile")
        val profile: PaymentOutLinkProfile?,
        @SerializedName("destination")
        val destination: PaymentOutLinkDestination?,
        @SerializedName("destination_update_allowed")
        val destinationUpdateAllowed: Boolean?,
    )

    data class PaymentOutLinkProfile(
        @SerializedName("message_link")
        val messageLink: String,
        @SerializedName("link_intent")
        val linkIntent: String,
        @SerializedName("linkVpa")
        val linkVpa: String,
        @SerializedName("link_id")
        val linkId: String,
    )

    data class PaymentOutLinkDestination(
        @SerializedName("name")
        val name: String,
        @SerializedName("mobile")
        val mobile: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("payment_address")
        val paymentAddress: String,
        @SerializedName("upi_vpa")
        val upiVpa: String,
    )

    data class TriggerMerchantPayoutRequest(
        @SerializedName("payment_type")
        val paymentType: String,
        @SerializedName("collection_type")
        val collectionType: String,
        @SerializedName("payout_id")
        val payoutId: String,
        @SerializedName("payment_id")
        val paymentId: String,
    )

    data class CollectionEventRequest(
        @SerializedName("account_id")
        val accountId: String?,
        @SerializedName("event_name")
        val eventName: String,
    )

    data class BlindPayCreateLinkRequest(
        @SerializedName("account_id")
        val accountId: String,
    )

    data class BlindPayCreateLinkResponse(
        @SerializedName("link_id")
        val linkId: String,
    )

    data class TargetedReferralResponse(
        @SerializedName("customers")
        val customers: List<TargetedReferralCustomerResponse>,
        @SerializedName("amount")
        val amount: Long,
        @SerializedName("message")
        val message: String,
        @SerializedName("youtube_link")
        val youtubeLink: String,
    )

    data class TargetedReferralCustomerResponse(
        @SerializedName("customer_id")
        val customerId: String,
        @SerializedName("link")
        val link: String,
        @SerializedName("status")
        val status: Int,
        @SerializedName("customer_merchant_id")
        val customerMerchantId: String,
    )

    data class ShareTargetedReferralRequest(
        @SerializedName("customer_merchant_id")
        val customerMerchantId: String,
    )

    data class CustomerPaymentIntentRequest(
        @SerializedName("merchant_id")
        val merchantId: String,
        @SerializedName("customer_id")
        val customerId: String,
        @SerializedName("payment_intent")
        val paymentIntent: String,
    )

    data class PaymentTagRequest(
        @SerializedName("payment_id")
        val paymentId: String? = null,
        @SerializedName("payment_type")
        val paymentType: String? = null,
        @SerializedName("tags")
        val tags: TagRequest,
        @SerializedName("timestamp")
        val timestamp: Long? = null,
    )

    data class TagRequest(
        @SerializedName("isViewed")
        val isViewed: String,
    )

    data class TagResponse(
        @SerializedName("isViewed")
        val isViewed: Boolean,
    )

    data class BlindPayShareLinkRequest(
        @SerializedName("payment_id")
        val paymentId: String = "",
        @SerializedName("collection_id")
        val collectionId: String = "",
        @SerializedName("resend_channel")
        val resendChannel: Int = 0,
    )

    data class BlindPayShareLinkResponse(
        @SerializedName("short_link")
        val shareLink: String,
    )

    data class DestinationRequest(
        @SerializedName("mobile")
        val mobile: String? = "",

        @SerializedName("name")
        val name: String? = "",

        @SerializedName("payment_address")
        val paymentAddress: String,

        @SerializedName("type")
        val type: String,

        @SerializedName("upi_vpa")
        val upiVpa: String? = "",
    )
}
