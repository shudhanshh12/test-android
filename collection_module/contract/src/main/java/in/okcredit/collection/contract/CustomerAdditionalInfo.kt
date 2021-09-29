package `in`.okcredit.collection.contract

data class CustomerAdditionalInfo(
    val id: String,
    val link: String,
    val status: Int,
    val amount: Long,
    val message: String,
    val youtubeLink: String,
    val customerMerchantId: String,
    val ledgerSeen: Boolean
)

enum class ReferralStatus(val value: Int) {
    DEFAULT(0),
    LINK_CREATED(1),
    LINK_SHARED(2),
    LINK_INVALID(3),
    DESTINATION_ADDED(4),
    PAYMENT_RECEIVED(5),
    REWARD_CREATED(6),
    REWARD_PROCESSING(7),
    REWARD_SUCCESS(8),
    REWARD_FAILED(9);

    companion object {
        val map = values().associateBy(ReferralStatus::value)

        fun fromValue(value: Int) = map[value] ?: LINK_CREATED
    }
}

const val COLLECTION_TARGET_REFERRAL = "collection_targeted_referral"
