package merchant.okcredit.gamification.ipl.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardComingSoonFragment
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardFragment
import merchant.okcredit.gamification.ipl.match.SelectMatchFragment
import merchant.okcredit.gamification.ipl.match.TodaysGameComingSoonFragment
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameFragment
import merchant.okcredit.gamification.ipl.sundaygame.WeeklyDrawComingSoonFragment

class IplViewPagerAdapter(
    private val fragment: Fragment,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) : FragmentPagerAdapter(
    fragment.childFragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    companion object {
        private const val CONFIG_TODAYS_GAME = "is_ipl_todays_game_enabled"
        private const val CONFIG_LEADERBOARD = "is_ipl_leaderboard_enabled"
        private const val CONFIG_WEEKLY_DRAW = "is_ipl_weekly_draw_enabled"
    }

    override fun getCount() = fragment.resources.getStringArray(R.array.ipl_tabs).size

    override fun getPageTitle(position: Int): String = fragment.resources.getStringArray(R.array.ipl_tabs)[position]

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                if (isTodaysGameEnabled()) {
                    SelectMatchFragment.newInstance()
                } else {
                    TodaysGameComingSoonFragment.newInstance()
                }
            }
            1 -> {
                if (isLeaderboardEnabled()) {
                    LeaderboardFragment.newInstance()
                } else {
                    LeaderboardComingSoonFragment.newInstance()
                }
            }
            2 -> {
                if (isWeeklyDrawEnabled()) {
                    SundayGameFragment.newInstance()
                } else {
                    WeeklyDrawComingSoonFragment.newInstance()
                }
            }
            else -> throw IllegalArgumentException("Only 3 tabs are supported $position")
        }
    }

    private fun isTodaysGameEnabled() = firebaseRemoteConfig.get().getBoolean(CONFIG_TODAYS_GAME)

    private fun isLeaderboardEnabled() = firebaseRemoteConfig.get().getBoolean(CONFIG_LEADERBOARD)

    private fun isWeeklyDrawEnabled() = firebaseRemoteConfig.get().getBoolean(CONFIG_WEEKLY_DRAW)
}
