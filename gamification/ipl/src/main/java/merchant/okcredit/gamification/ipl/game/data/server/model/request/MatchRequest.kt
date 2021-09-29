package merchant.okcredit.gamification.ipl.game.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MatchRequest(
    @SerializedName("match_id")
    val matchId: String
)
