package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class GetMysteryCardsResponse(
    @SerializedName("prizes")
    val prizes: List<MysteryPrizeModel>
)
