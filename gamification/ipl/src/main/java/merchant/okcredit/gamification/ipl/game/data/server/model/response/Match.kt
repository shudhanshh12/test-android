package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class Match(
    @SerializedName("start_time")
    val startTime: Long = 0,
    @SerializedName("id")
    val id: String = "",
    @SerializedName("home_team")
    val homeTeam: Team,
    @SerializedName("away_team")
    val awayTeam: Team,
    @SerializedName("series_name")
    val seriesName: String
)
