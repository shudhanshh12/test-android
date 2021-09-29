package merchant.okcredit.gamification.ipl.model

import androidx.annotation.Keep

@Keep
data class PlayerScore(
    val batting_runs: Int? = null,
    val batting_balls: Int? = null,
    val bowling_wickets: Int? = null,
    val bowling_overs: Int? = null,
    val bowling_balls: Int? = null,
    val batting_state: String = "",
    val bowling_state: String = ""
)
