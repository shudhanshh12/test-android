package merchant.okcredit.gamification.ipl.model

import androidx.annotation.Keep

@Keep
data class MatchStatus(
    val status: String = "",
    val winning_team_id: String = ""
)
