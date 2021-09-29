package merchant.okcredit.gamification.ipl.game.utils

import androidx.annotation.StringRes
import merchant.okcredit.gamification.ipl.R

enum class BowlingStatus constructor(val status: String, @StringRes val resource: Int) {

    BOWLING("BOWLING", R.string.status_bowling),
    NOT_BOWLING("NOT_BOWLING", R.string.status_not_bowling),
    YET_TO_BOWL("YET_TO_BOWL", R.string.status_yet_to_bowl);

    companion object {

        val map = values().associateBy(BowlingStatus::status)

        fun fromStatus(status: String) = map[status] ?: YET_TO_BOWL
    }
}
