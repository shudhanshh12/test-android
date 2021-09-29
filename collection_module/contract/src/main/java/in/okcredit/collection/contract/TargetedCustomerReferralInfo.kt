package `in`.okcredit.collection.contract

data class TargetedCustomerReferralInfo(
    val id: String,
    val mobile: String? = null,
    val profileImage: String? = null,
    val description: String = "",
    val link: String,
    val status: Int,
    val amount: Long,
    val message: String,
    val youtubeLink: String,
    val customerMerchantId: String,
    val ledgerSeen: Boolean,
)
