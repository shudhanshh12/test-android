package tech.okcredit.android.referral.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.referral.contract.utils.ReferralVersion
import dagger.Lazy
import javax.inject.Inject

class ReferralEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        private const val REFERRAL_REWARD_SCREEN_OPENED = "Referral Reward Screen Opened"
        private const val EARN_MORE_REWARDS = "Referral See more details clicked"
        private const val NOTIFY_MERCHANT = "Notify button clicked"

        private const val UNCLAIMED_REWARDS = "Claim Referral amount button Clicked"
        private const val REFERRED_USER_QUALIFICATION = "Referred User Qualification"
        const val REFERRAL_SHARE = "Share Referral"
        const val VIEW_REFERRAL = "View Referral"
        private const val COLLECTION_DIALOG = "Set Up Collection Dialog"
        private const val REFERRAL_SCREEN = "Referral Screen"
        const val VERSION = "V3"
        val DEFAULT_PROPERTY = mapOf(
            PropertyKey.REFERRAl_VERSION to VERSION
        )

        private const val SHARE_APP_SCREEN = "Share App Screen"
    }

    object PropertyKey {
        const val QUALIFIED = "qualified"
        const val ITEM = "item"
        const val REFERRAl_VERSION = "referrer_version"
        const val TYPE = "type"
    }

    fun trackReferralRewardsViewed(version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(REFERRAL_REWARD_SCREEN_OPENED, properties)
    }

    fun trackEarnMoreRewardsInteracted(version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(EARN_MORE_REWARDS, properties)
    }

    fun trackNotifyMerchantInteracted(version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(NOTIFY_MERCHANT, properties)
    }

    fun trackUnclaimedRewardsInteracted(version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(UNCLAIMED_REWARDS, properties)
    }

    fun trackReferredUserQualification(qualified: Int) {
        val properties = mapOf(PropertyKey.QUALIFIED to qualified)
        analyticsProvider.get().trackEvents(REFERRED_USER_QUALIFICATION, properties)
    }

    fun trackCollectionInteracted(item: String, version: ReferralVersion) {
        val properties = mapOf(
            PropertyKey.TYPE to version.type,
            PropertyKey.ITEM to item
        )
        analyticsProvider.get().trackObjectInteracted(COLLECTION_DIALOG, InteractionType.CLICK, properties)
    }

    fun trackCollectionDismissed() {
        analyticsProvider.get().trackObjectInteracted(COLLECTION_DIALOG, InteractionType.DISMISS)
    }

    fun trackReferralScreenViewed(version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackObjectViewed(REFERRAL_SCREEN, properties)
    }

    fun trackReferralScreenInteracted(
        item: String,
        version: ReferralVersion,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, version.type)
            put(PropertyKey.ITEM, item)
        }
        analyticsProvider.get().trackObjectInteracted(REFERRAL_SCREEN, interactionType, properties)
    }

    fun trackShareReferral(screen: String, version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(`in`.okcredit.analytics.PropertyKey.SCREEN, screen)
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(REFERRAL_SHARE, properties)
    }

    fun trackReferralInApp(type: String) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.TYPE, type)
        }
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTI_DISPLAYED, properties)
    }

    fun trackSharedOnWhatsApp() {
        analyticsProvider.get().trackEvents(Event.SHARED_ON_WHATSAPP_CLICK)
    }

    fun trackApkSharedOnWhatsApp() {
        analyticsProvider.get().trackEvents(Event.APK_SHARED_ON_WHATSAPP_CLICK)
    }

    fun trackShareAppViewed() {
        analyticsProvider.get().trackObjectViewed(SHARE_APP_SCREEN)
    }

    fun trackShareAppInteracted(item: String, interactionType: InteractionType = InteractionType.CLICK) {
        val properties = mapOf(PropertyKey.ITEM to item)
        analyticsProvider.get().trackObjectInteracted(SHARE_APP_SCREEN, interactionType, properties)
    }
}
