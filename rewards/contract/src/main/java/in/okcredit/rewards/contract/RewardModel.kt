package `in`.okcredit.rewards.contract

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class RewardModel(
    val id: String,
    val create_time: DateTime,
    val update_time: DateTime,
    val status: String,
    val reward_type: String?,
    val amount: Long,
    val featureName: String,
    val featureTitle: String,
    val description: String,
    val deepLink: String,
    val icon: String,
    val labels: Map<String, String>, // use getter getLabelByKey instead
    val createdBy: String,
) : Parcelable {

    object LabelKeys {
        const val PAYMENT_ID = "payment_id" // sent for okp_cashback rewards
        const val ACCOUNT_ID = "account_id" // sent for okp_cashback rewards

        val allowedLabelKeys = listOf(
            PAYMENT_ID, ACCOUNT_ID
        )
    }

    fun getLabelByKey(key: String): String {
        return if (key in LabelKeys.allowedLabelKeys) labels[key] ?: "" else ""
    }

    fun isClaimed() = this.status.startsWith("claimed")

    fun isUnclaimed() = this.status.startsWith("unclaimed")

    fun isOnHold() = this.status.startsWith("on_hold")

    fun isProcessing() = this.status.startsWith("processing")

    fun isFailed() = this.status.startsWith("failed")

    fun isEditBankDetails() = this.status.contains("on_hold/bank/user_details/duplicate") ||
        this.status.contains("on_hold/bank/user_details/inactive")

    fun isBetterLuckNextTimeReward() = RewardType.BETTER_LUCK_NEXT_TIME == RewardType.fromString(reward_type ?: "")

    fun isFeatureRewards() = RewardType.ACTIVATION_FEATURE_REWARDS == RewardType.fromString(reward_type ?: "")

    fun isNonCashReward() = RewardType.NON_CASH_REWARDS.contains(RewardType.fromString(reward_type ?: ""))
}

fun RewardModel.getAmountInRupees() = amount / 100
