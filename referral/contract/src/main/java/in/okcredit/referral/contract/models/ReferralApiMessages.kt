package `in`.okcredit.referral.contract.models

import com.google.gson.annotations.SerializedName

interface ReferralApiMessages {

    data class GetTargetedUsersResponse(
        @SerializedName("targetedUsers")
        val targetUsers: List<TargetedUser>
    )

    data class GetShareContentResponse(
        @SerializedName("targeted_content")
        val targetContent: ShareContent?,
        @SerializedName("generic_content")
        val genericContent: ShareContent
    )
}
