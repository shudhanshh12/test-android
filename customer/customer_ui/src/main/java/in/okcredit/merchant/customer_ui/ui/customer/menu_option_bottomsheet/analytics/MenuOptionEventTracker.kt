package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.referral.contract.utils.ReferralVersion
import dagger.Lazy
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import javax.inject.Inject

class MenuOptionEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        const val MENU_OPTION_BOTTOM_SHEET = "MenuOptionBottomSheet"
        const val REFERRAL_SHARE = "Share Referral"
        const val VIEW_REFERRAL = "View Referral"
        private val DEFAULT_PROPERTY = mapOf(
            `in`.okcredit.analytics.PropertyKey.VERSION to "V3"
        )
    }

    object PropertyKey {
        const val ITEM = "item"
        const val TYPE = "type"
    }

    fun trackMenuOptionViewed(property: Map<String, Any>? = emptyMap()) =
        analyticsProvider.get().trackObjectViewed(MENU_OPTION_BOTTOM_SHEET, property)

    fun trackViewReferral(item: String, version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(PropertyKey.ITEM, item)
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(VIEW_REFERRAL, properties)
    }

    fun trackInviteShareReferral(version: ReferralVersion) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(`in`.okcredit.analytics.PropertyKey.SCREEN, MENU_OPTION_BOTTOM_SHEET)
            put(PropertyKey.TYPE, version.type)
        }
        analyticsProvider.get().trackEvents(REFERRAL_SHARE, properties)
    }

    fun trackMenuScreenInteracted(
        item: String,
        version: ReferralVersion,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = DEFAULT_PROPERTY.toMutableMap().apply {
            put(`in`.okcredit.analytics.PropertyKey.TYPE, version.type)
            put(ReferralEventTracker.PropertyKey.ITEM, item)
        }
        analyticsProvider.get().trackObjectInteracted(
            MENU_OPTION_BOTTOM_SHEET,
            interactionType,
            properties
        )
    }
}
