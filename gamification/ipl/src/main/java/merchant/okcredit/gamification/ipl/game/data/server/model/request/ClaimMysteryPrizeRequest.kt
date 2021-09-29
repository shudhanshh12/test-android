package merchant.okcredit.gamification.ipl.game.data.server.model.request

import com.google.gson.annotations.SerializedName

data class ClaimMysteryPrizeRequest(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("prize_id")
    val prizeId: String
)
