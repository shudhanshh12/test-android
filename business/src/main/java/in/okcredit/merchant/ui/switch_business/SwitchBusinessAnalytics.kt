package `in`.okcredit.merchant.ui.switch_business

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.merchant.contract.BusinessEvents
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_SCREEN
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_SOURCE
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_TARGET_BUSINESS_ID
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_TYPE
import `in`.okcredit.merchant.contract.BusinessEvents.Value.MERCHANT
import `in`.okcredit.merchant.contract.BusinessEvents.Value.SCREEN
import dagger.Lazy
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

class SwitchBusinessAnalytics @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {

    fun trackPageViewed(source: String) {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.SELECT_BUSINESS_PAGE_VIEWED,
            properties = mapOf(KEY_SOURCE to source)
        )
    }

    fun trackBusinessSelected(source: String, targetBusinessId: String) {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.BUSINESS_SELECTED,
            properties = mapOf(
                KEY_SOURCE to source,
                KEY_TARGET_BUSINESS_ID to targetBusinessId
            )
        )
    }

    fun trackViewProfile() {
        analyticsProvider.get().trackEvents(
            BusinessEvents.VIEW_PROFILE,
            properties = mapOf(
                KEY_TYPE to MERCHANT,
                KEY_SCREEN to SCREEN
            )
        )
    }

    fun trackCreateNewBusinessStarted(source: String) {
        analyticsProvider.get().trackEvents(
            BusinessEvents.CREATE_BUSINESS_STARTED,
            properties = mapOf(
                KEY_SOURCE to source
            )
        )
    }

    fun trackAutoSwitchedBusiness(source: String, targetBusinessId: String) {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.AUTO_SWITCHED_BUSINESS,
            properties = mapOf(
                KEY_SOURCE to source,
                KEY_TARGET_BUSINESS_ID to targetBusinessId
            )
        )
    }

    fun trackSwitchBusinessError(throwable: Throwable) {
        analyticsProvider.get().trackEngineeringMetricEvents(
            eventName = BusinessEvents.SWITCH_BUSINESS_ERROR,
            properties = mapOf(
                PropertyValue.REASON to throwable.message.toString(),
                PropertyValue.CAUSE to throwable.cause?.message.toString(),
                PropertyValue.STACKTRACE to throwable.getStringStackTrace()
            )
        )
    }
}
