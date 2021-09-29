package `in`.okcredit.merchant.ui.select_business

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.merchant.contract.BusinessEvents
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_SOURCE
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_TARGET_BUSINESS_ID
import `in`.okcredit.merchant.contract.BusinessEvents.Value.POST_LOGIN_FLOW
import dagger.Lazy
import javax.inject.Inject

class SelectBusinessAnalytics @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {

    fun trackPageViewed() {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.SELECT_BUSINESS_PAGE_VIEWED,
            properties = mapOf(KEY_SOURCE to POST_LOGIN_FLOW)
        )
    }

    fun trackBusinessSelected(targetBusinessId: String) {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.BUSINESS_SELECTED,
            properties = mapOf(
                KEY_SOURCE to POST_LOGIN_FLOW,
                KEY_TARGET_BUSINESS_ID to targetBusinessId
            )
        )
    }
}
