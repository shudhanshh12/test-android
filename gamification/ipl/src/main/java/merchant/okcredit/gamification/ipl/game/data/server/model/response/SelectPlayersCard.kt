package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class SelectPlayersCard(
    @SerializedName("players")
    val allPlayers: List<Player>,
    @SerializedName("id")
    val id: String = "",
    @SerializedName("choice")
    val chosenPlayer: Player? = null
)
