package merchant.okcredit.gamification.ipl.game.data.server.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MysteryPrizeModel(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("created")
    val created: Long,
    @SerializedName("id")
    val id: String,
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: Long
) : Parcelable {

    fun isClaimed() = status == MysteryPrizeStatus.CLAIMED.status

    fun isWelcomeReward() = type == MysteryRewardSubType.WELCOME_REWARD.status
}
