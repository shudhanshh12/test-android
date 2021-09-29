package merchant.okcredit.gamification.ipl.game.data.server.model.request

import com.google.gson.annotations.SerializedName

data class ChoiceRequest(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("match_id")
    val matchId: String,
    @SerializedName("choice_id")
    val choiceId: String
)
