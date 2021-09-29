package tech.okcredit.android.referral.ui.rewards_on_signup.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.referral.contract.RewardsOnSignupTracker
import dagger.Lazy
import javax.inject.Inject

class RewardsOnSignupTrackerImpl @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) : RewardsOnSignupTracker {

    companion object {
        const val REFERRAL_FULL_VIEW = "Referral Full View"
        const val REFERRAL_TARGET_BANNER = "Referral Target Banner"
    }

    object PropertyKey {
        const val ITEM = "Item"
    }

    override fun trackTargetBannerViewed() {
        analyticsProvider.get().trackObjectViewed(REFERRAL_TARGET_BANNER)
    }

    override fun trackFullBannerViewed() {
        analyticsProvider.get().trackObjectViewed(REFERRAL_FULL_VIEW)
    }

    override fun trackTargetBannerInteracted(item: String) {
        val properties = mapOf<String, Any>(
            PropertyKey.ITEM to item
        )
        analyticsProvider.get().trackObjectInteracted(REFERRAL_TARGET_BANNER, InteractionType.CLICK, properties)
    }
}
