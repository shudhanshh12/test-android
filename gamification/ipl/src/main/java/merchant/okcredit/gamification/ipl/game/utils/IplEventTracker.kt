package merchant.okcredit.gamification.ipl.game.utils

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import javax.inject.Inject

class IplEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Property {
        const val SOURCE = "Source"
        const val SCREEN = "Screen"
        const val STATE = "State"

        const val TYPE = "Type"
        const val SUB_TYPE = "SubType"
        const val ERROR_CODE = "Code"
        const val CAUSE = "Cause"
        const val INPUT_TEXT = "InputText"
    }

    object Value {
        const val TODAYS_TAB_SCREEN = "Today's tab"
        const val SUNDAY_TAB_SCREEN = "Weekly tab"
        const val LEADERBOARD_SCREEN = "Leaderboard tab"
        const val MATCH_ONBOARDING = "Match Onboarding"
        const val GAME_SCREEN = "Game Screen"
        const val FIRST_QUESTION_ANSWERED = "First Question Answered"
        const val SECOND_QUESTION_ANSWERED = "Second Question Answered"
        const val THIRD_QUESTION_ANSWERED = "Third Question Answered"
        const val SOURCE_IPL_REWARDS = "Ipl Rewards"
        const val SOURCE_BOOSTER_QUESTION = "Ipl Booster Question"
    }

    fun matchScreenClicked() {
        analyticsProvider.get().trackEvents("Today's Game Tab Clicked")
    }

    fun matchScreenViewed() {
        analyticsProvider.get().trackEvents("Today's Game Tab Viewed")
    }

    fun matchOnboardingViewed() {
        analyticsProvider.get().trackEvents("Match Onboarding Viewed")
    }

    fun weeklyDrawViewed() {
        analyticsProvider.get().trackEvents("Weekly Draw Tab Viewed")
    }

    fun leaderBoardViewed() {
        analyticsProvider.get().trackEvents("Leaderboard Tab Viewed")
    }

    fun matchSelected() {
        analyticsProvider.get().trackEvents("Game Selected")
    }

    fun rewardsViewed(source: String) {
        val properties = mapOf(Property.SOURCE to source)
        analyticsProvider.get().trackEvents("Prize Card Displayed", properties)
    }

    fun rewardClicked(source: String, rewardType: String) {
        val properties = mapOf(
            Property.SOURCE to source,
            Property.TYPE to rewardType
        )
        analyticsProvider.get().trackEvents("Prize Card Clicked", properties)
    }

    fun rewardClaimed(source: String, rewardType: String) {
        val properties = mapOf(
            Property.SOURCE to source,
            Property.TYPE to rewardType
        )
        analyticsProvider.get().trackEvents("Prize Collected", properties)
    }

    fun weeklyDrawClicked() {
        analyticsProvider.get().trackEvents("Weekly Draw Tab Clicked")
    }

    fun leaderBoardClicked() {
        analyticsProvider.get().trackEvents("Leaderboard Tab Clicked")
    }

    fun mysteryPrizeViewed(source: String) {
        val properties = mapOf(Property.SOURCE to source)
        analyticsProvider.get().trackEvents("Mystery Gifts Displayed", properties)
    }

    fun mysteryPrizeClicked(source: String) {
        val properties = mapOf(Property.SOURCE to source)
        analyticsProvider.get().trackEvents("Mystery Gifts Clicked", properties)
    }

    fun mysteryPrizeClaimed(source: String) {
        val properties = mapOf(Property.SOURCE to source)
        analyticsProvider.get().trackEvents("Mystery Gifts Collected", properties)
    }

    fun boosterCardDisplayed(type: Int, subType: Int) {
        val properties = mapOf(Property.TYPE to type, Property.SUB_TYPE to subType)
        analyticsProvider.get().trackEvents("Booster Card Displayed", properties)
    }

    fun boosterCardClicked(type: String, subType: String) {
        val properties = mapOf(Property.TYPE to type, Property.SUB_TYPE to subType)
        analyticsProvider.get().trackEvents("Booster Card Clicked", properties)
    }

    fun gameQuestionAnswered(questionNumber: String) {
        analyticsProvider.get().trackEvents(questionNumber)
    }

    fun networkError(screen: String, code: Int, cause: String? = null) {
        val properties = mapOf(
            Property.TYPE to "Network Error",
            Property.SCREEN to screen,
            Property.ERROR_CODE to code,
            Property.CAUSE to if (cause.isNullOrBlank()) "" else cause
        )
        analyticsProvider.get().trackEvents("Ipl Error", properties)
    }

    fun serverError(screen: String, code: Int, cause: String? = null) {
        val properties = mapOf(
            Property.TYPE to "Server Error",
            Property.SCREEN to screen,
            Property.ERROR_CODE to code,
            Property.CAUSE to if (cause.isNullOrBlank()) "" else cause
        )
        analyticsProvider.get().trackEvents("Ipl Error", properties)
    }

    fun youtubeSelected(screen: String) {
        val properties = mapOf(
            Property.SCREEN to screen
        )
        analyticsProvider.get().trackEvents("IPL Youtube Played", properties)
    }

    fun gameRuleClosed(screen: String) {
        val properties = mapOf(
            Property.SCREEN to screen,
            Property.STATE to "Closed"
        )
        analyticsProvider.get().trackEvents("IPL Game Rules Clicked", properties)
    }

    fun gameRuleOpened(screen: String) {
        val properties = mapOf(
            Property.SCREEN to screen,
            Property.STATE to "Opened"
        )
        analyticsProvider.get().trackEvents("IPL Game Rules Clicked", properties)
    }
}
