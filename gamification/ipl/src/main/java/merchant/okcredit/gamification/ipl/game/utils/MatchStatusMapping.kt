package merchant.okcredit.gamification.ipl.game.utils

import androidx.annotation.StringRes
import merchant.okcredit.gamification.ipl.R

enum class MatchStatusMapping constructor(val status: String, @StringRes val resource: Int) {

    IN_PLAY("IN_PLAY", R.string.live),
    TEA("TEA", R.string.status_tea),
    LUNCH("LUNCH", R.string.status_lunch),
    BETWEEN_INNINGS("BETWEEN INNINGS", R.string.status_2nd_innings),
    RAIN_DELAY("RAIN DELAY", R.string.status_rain),
    BAD_LIGHT("BAD_LIGHT", R.string.status_bad_light),
    CROWD_TROUBLE("CROWD TROUBLE", R.string.status_interrupted),
    PITCH_CONDITION("PITCH CONDITION", R.string.status_interrupted),
    ABANDONED("ABANDONED", R.string.status_interrupted),
    FLOODLIGHT_FAILURE("FLOODLIGHT_FAILURE", R.string.status_interrupted),
    PLAY_SUSPENDED_UNKNOWN("PLAY_SUSPENDED_UNKNOWN", R.string.status_interrupted),
    START_DELAYED("START_DELAYED", R.string.status_delayed),
    DRINKS("DRINKS", R.string.status_drinks),
    SUPER_OVER("SUPER_OVER", R.string.status_super_over),
    STUMPS("STUMPS", R.string.status_day_over),
    MATCH_FINISHED("MATCH_FINISHED", R.string.status_match_over),
    UNKNOWN("UNKNOWN", R.string.status_nil);

    companion object {

        val map = values().associateBy(MatchStatusMapping::status)

        fun fromStatus(status: String) = map[status] ?: UNKNOWN
    }
}
