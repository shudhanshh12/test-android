package `in`.okcredit.merchant.rewards.server.internal

import `in`.okcredit.rewards.contract.RewardModel
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import org.joda.time.DateTime

interface ApiMessages {

    /*
     * Used for deserialization from backend
     */
    @Keep
    @JsonClass(generateAdapter = true)
    data class RewardFromApi(
        val id: String,
        val create_time: DateTime,
        val update_time: DateTime,
        val status: String,
        val amount: Long,
        val reward_type: String,
        val feature_details: FeatureReward? = null,
        val labels: Map<String, String>,
        val created_by: String,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class FeatureReward(
        val feature_name: String,
        val title: String,
        val description: String,
        val deep_link: String?,
        val icon_link: String?,
    )

    object ListRewardApiRequest

    @Keep
    data class ListRewardsApiResponse(
        val rewards: List<RewardFromApi>,
    )

    @Keep
    data class ClaimRewardRequest(
        val reward_id: String,
        val lang: String,
    )

    @Keep
    data class ClaimRewardResponse(
        val status: String,
        val custom_message: String? = null, // will only be utilized it status ends in `custom`
    )
}

fun ApiMessages.RewardFromApi.toRewardModel() =
    RewardModel(
        id = this.id,
        create_time = this.create_time,
        update_time = this.update_time,
        status = this.status,
        reward_type = this.reward_type,
        amount = this.amount,
        featureName = this.feature_details?.feature_name ?: "",
        featureTitle = this.feature_details?.title ?: "",
        description = this.feature_details?.description ?: "",
        deepLink = this.feature_details?.deep_link ?: "",
        icon = this.feature_details?.icon_link ?: "",
        labels = this.labels,
        createdBy = this.created_by
    )
