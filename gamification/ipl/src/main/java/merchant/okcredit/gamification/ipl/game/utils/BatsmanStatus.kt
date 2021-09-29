package merchant.okcredit.gamification.ipl.game.utils

import androidx.annotation.StringRes
import merchant.okcredit.gamification.ipl.R

enum class BatsmanStatus constructor(val status: String, @StringRes val resource: Int) {

    NOT_OUT("NOT OUT", R.string.status_batting),
    BOWLED("BOWLED", R.string.status_bowled),
    CAUGHT_AND_BOWLED("CAUGHT AND BOWLED", R.string.status_caught_and_bowled),
    CAUGHT("CAUGHT", R.string.status_caught),
    HANDLED_THE_BALL("HANDLED THE BALL", R.string.status_out),
    HIT_BALL_TWICE("HIT BALL TWICE", R.string.status_out),
    HIT_WICKET("HIT WICKET", R.string.status_hit_wicket),
    LBW("LBW", R.string.status_lbw),
    OBSTRUCTING_THE_FIELD("OBSTRUCTING THE FIELD", R.string.status_out),
    RETIRED_HURT("RETIRED HURT", R.string.status_retired_hurt),
    RETIRED_OUT("RETIRED OUT", R.string.status_out),
    RUN_OUT("RUN OUT", R.string.status_run_out),
    STUMPED("STUMPED", R.string.status_stumped),
    TIMED_OUT("TIMED OUT", R.string.status_out),
    CAUGHT_SUB("CAUGHT (SUB)", R.string.status_caught),
    RUN_OUT_SUB("RUN OUT (SUB)", R.string.status_run_out),
    ABSENT_HURT("ABSENT HURT", R.string.status_out),
    RETIRED_NOT_OUT("RETIRED NOT OUT", R.string.status_out),
    STUMPED_SUB("STUMPED (SUB)", R.string.status_stumped),
    YET_TO_BAT("YET TO BAT", R.string.status_yet_to_bat),
    UNKNOWN("UNKNOWN", R.string.status_nil);

    companion object {

        val map = values().associateBy(BatsmanStatus::status)

        fun fromStatus(status: String) = map[status] ?: UNKNOWN
    }
}
