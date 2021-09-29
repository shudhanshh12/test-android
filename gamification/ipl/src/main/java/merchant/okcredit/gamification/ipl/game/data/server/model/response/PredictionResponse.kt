package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("match_prediction")
    val matchPrediction: MatchPrediction
)

data class MatchPrediction(
    @SerializedName("match_id")
    val matchId: String = "",
    @SerializedName("home_team")
    val homeTeam: Team,
    @SerializedName("away_team")
    val awayTeam: Team
)
