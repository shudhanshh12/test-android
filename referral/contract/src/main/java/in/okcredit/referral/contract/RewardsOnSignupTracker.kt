package `in`.okcredit.referral.contract

interface RewardsOnSignupTracker {
    fun trackTargetBannerViewed()

    fun trackFullBannerViewed()

    fun trackTargetBannerInteracted(item: String)
}
