package `in`.okcredit.collection.contract

import java.io.Serializable

data class CollectionCustomerProfile(
    val accountId: String,
    val message_link: String? = "",
    val message: String? = "",
    val link_intent: String? = "",
    val qr_intent: String? = "",
    val show_image: Boolean = false,
    val isSupplier: Boolean = false,
    val name: String? = "",
    val mobile: String? = "",
    val linkVpa: String? = "",
    val type: String? = "",
    val paymentAddress: String? = "",
    val upiVpa: String? = "",
    val fromMerchantPaymentLink: String? = "",
    val fromMerchantUpiIntent: String? = "",
    val linkId: String? = "",
    val googlePayEnabled: Boolean = false, // checks if we can enable GPay button on customer screen
    val paymentIntent: Boolean = false, // checks if customer has request for bank details
    val destinationUpdateAllowed: Boolean = true,
    val cashbackEligible: Boolean = false, // to get cashback eligible customer
) : Serializable
