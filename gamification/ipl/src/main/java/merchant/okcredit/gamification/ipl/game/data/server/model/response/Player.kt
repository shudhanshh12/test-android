package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("team_type")
    val teamType: String = ""

) {
    fun isHomeTeam() = teamType == HOME_TEAM
    fun isAwayTeam() = teamType == AWAY_TEAM

    companion object {
        const val HOME_TEAM = "0"
        const val AWAY_TEAM = "1"
    }
}
