package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class SelectTeamCard(
    @SerializedName("prediction")
    val prediction: Int = 0,
    @SerializedName("home_team")
    val homeTeam: Team,
    @SerializedName("away_team")
    val awayTeam: Team,
    @SerializedName("choice")
    val chosenTeam: Team? = null,
    @SerializedName("series_name")
    val seriesName: String = "",
    @SerializedName("match_number")
    val matchNumber: Int,
    @SerializedName("start_time")
    val startTime: Long = 0
)
