package merchant.okcredit.gamification.ipl.game.utils

import `in`.okcredit.analytics.AnalyticsProvider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class IplEventTrackerTest {

    private val analyticsProvider: AnalyticsProvider = mock()
    private val iplEventTracker = IplEventTracker { analyticsProvider }

    @Test
    fun `should call track event with correct name when matchScreenClicked is called`() {
        iplEventTracker.matchScreenClicked()

        verify(analyticsProvider).trackEvents(
            "Today's Game Tab Clicked", null
        )
    }

    @Test
    fun `should call track event with correct name when matchScreenViewed is called`() {
        iplEventTracker.matchScreenViewed()

        verify(analyticsProvider).trackEvents(
            "Today's Game Tab Viewed", null
        )
    }

    @Test
    fun `should call track event with correct name when matchOnboardingViewed is called`() {
        iplEventTracker.matchOnboardingViewed()

        verify(analyticsProvider).trackEvents(
            "Match Onboarding Viewed", null
        )
    }

    @Test
    fun `should call track event with correct name when matchSelected is called`() {
        iplEventTracker.matchSelected()

        verify(analyticsProvider).trackEvents(
            "Game Selected", null
        )
    }

    @Test
    fun `should call track event with correct name when rewardsViewed is called`() {
        iplEventTracker.rewardsViewed("Weekly tab")

        verify(analyticsProvider).trackEvents(
            "Prize Card Displayed",
            mapOf(
                "Source" to "Weekly tab"
            )
        )
    }

    @Test
    fun `should call track event with correct name when rewardClicked is called`() {
        iplEventTracker.rewardClicked("Weekly tab", "ipl_rewards")

        verify(analyticsProvider).trackEvents(
            "Prize Card Clicked",
            mapOf(
                "Source" to "Weekly tab",
                "Type" to "ipl_rewards"
            )
        )
    }

    @Test
    fun `should call track event with correct name when rewardClaimed is called`() {
        iplEventTracker.rewardClaimed("Weekly tab", "ipl_rewards")

        verify(analyticsProvider).trackEvents(
            "Prize Collected",
            mapOf(
                "Source" to "Weekly tab",
                "Type" to "ipl_rewards"
            )
        )
    }

    @Test
    fun `should call track event with correct name when weeklyDrawClicked is called`() {
        iplEventTracker.weeklyDrawClicked()

        verify(analyticsProvider).trackEvents("Weekly Draw Tab Clicked")
    }

    @Test
    fun `should call track event with correct name when leaderBoardClicked is called`() {
        iplEventTracker.leaderBoardClicked()

        verify(analyticsProvider).trackEvents("Leaderboard Tab Clicked")
    }

    @Test
    fun `should call track event with correct name when mysteryPrizeViewed is called`() {
        iplEventTracker.mysteryPrizeViewed("Weekly_Tab")

        verify(analyticsProvider).trackEvents(
            "Mystery Gifts Displayed",
            mapOf(
                "Source" to "Weekly_Tab"
            )
        )
    }

    @Test
    fun `should call track event with correct name when mysteryPrizeClicked is called`() {
        iplEventTracker.mysteryPrizeClicked("Weekly_Tab")

        verify(analyticsProvider).trackEvents(
            "Mystery Gifts Clicked",
            mapOf(
                "Source" to "Weekly_Tab"
            )
        )
    }

    @Test
    fun `should call track event with correct name when mysteryPrizeClaimed is called`() {
        iplEventTracker.mysteryPrizeClaimed("Weekly_Tab")

        verify(analyticsProvider).trackEvents(
            "Mystery Gifts Collected",
            mapOf(
                "Source" to "Weekly_Tab"
            )
        )
    }

    // Todo Event Should be Fixed to relevant Info
    @Test
    fun `should call track event with correct name when boosterCardDisplayed is called`() {
        iplEventTracker.boosterCardDisplayed(1, 2)

        verify(analyticsProvider).trackEvents(
            "Booster Card Displayed",
            mapOf(
                "Type" to 1,
                "SubType" to 2
            )
        )
    }

    @Test
    fun `should call track event with correct name when boosterCardClicked is called`() {
        iplEventTracker.boosterCardClicked("MCQ", "Add Custome")

        verify(analyticsProvider).trackEvents(
            "Booster Card Clicked",
            mapOf(
                "Type" to "MCQ",
                "SubType" to "Add Custome"
            )
        )
    }

    @Test
    fun `should call track event with correct name when networkError is called`() {
        iplEventTracker.networkError(
            IplEventTracker.Value.LEADERBOARD_SCREEN,
            501,
            "Internal Server error"
        )

        verify(analyticsProvider).trackEvents(
            "Ipl Error",
            mapOf(
                "Type" to "Network Error",
                "Screen" to "Leaderboard tab",
                "Code" to 501,
                "Cause" to "Internal Server error"
            )
        )
    }

    @Test
    fun `should call track event with correct name when gameQuestionAnswered is called`() {
        iplEventTracker.gameQuestionAnswered(IplEventTracker.Value.FIRST_QUESTION_ANSWERED)

        verify(analyticsProvider).trackEvents(
            "First Question Answered"
        )
    }

    @Test
    fun `should call track event with correct name when youtubeSelected is called`() {
        iplEventTracker.youtubeSelected(IplEventTracker.Value.TODAYS_TAB_SCREEN)

        verify(analyticsProvider).trackEvents(
            "IPL Youtube Played",
            mapOf(
                "Screen" to "Today's tab"
            )
        )
    }

    @Test
    fun `should call track event with correct name when gameRuleClosed is called`() {
        iplEventTracker.gameRuleClosed(IplEventTracker.Value.TODAYS_TAB_SCREEN)

        verify(analyticsProvider).trackEvents(
            "IPL Game Rules Clicked",
            mapOf(
                "Screen" to "Today's tab",
                "State" to "Closed"
            )
        )
    }

    @Test
    fun `should call track event with correct name when gameRuleOpened is called`() {
        iplEventTracker.gameRuleOpened(IplEventTracker.Value.TODAYS_TAB_SCREEN)

        verify(analyticsProvider).trackEvents(
            "IPL Game Rules Clicked",
            mapOf(
                "Screen" to "Today's tab",
                "State" to "Opened"
            )
        )
    }
}
