package merchant.okcredit.gamification.ipl.model

import androidx.annotation.Keep

@Keep
data class TeamScore(val runs: Int? = null, val wickets: Int? = null, val overs: Int? = null, val balls: Int? = null)

fun TeamScore?.isValid() = this != null && runs != null && wickets != null && overs != null && balls != null
