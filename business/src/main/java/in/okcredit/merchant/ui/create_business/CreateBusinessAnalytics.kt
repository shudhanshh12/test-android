package `in`.okcredit.merchant.ui.create_business

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.merchant.contract.BusinessEvents
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_FLOW
import `in`.okcredit.merchant.contract.BusinessEvents.Key.KEY_flow
import `in`.okcredit.merchant.contract.BusinessEvents.Value.CREATE_NEW_BUSINESS_FLOW
import dagger.Lazy
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

class CreateBusinessAnalytics @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {
    fun trackNameEntered() {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.NAME_ENTERED,
            properties = mapOf(KEY_FLOW to CREATE_NEW_BUSINESS_FLOW)
        )
    }

    fun trackNameEnteredSuccessful() {
        analyticsProvider.get().trackEvents(
            eventName = BusinessEvents.NAME_ENTERED_SUCCESSFUL,
            properties = mapOf(KEY_flow to CREATE_NEW_BUSINESS_FLOW)
        )
    }

    fun trackCreateBusinessError(throwable: Throwable) {
        analyticsProvider.get().trackEngineeringMetricEvents(
            eventName = BusinessEvents.CREATE_BUSINESS_ERROR,
            properties = mapOf(
                PropertyValue.REASON to throwable.message.toString(),
                PropertyValue.CAUSE to throwable.cause?.message.toString(),
                PropertyValue.STACKTRACE to throwable.getStringStackTrace()
            )
        )
    }

    fun trackCreateBusinessError(reason: String) {
        analyticsProvider.get().trackEngineeringMetricEvents(
            eventName = BusinessEvents.CREATE_BUSINESS_ERROR,
            properties = mapOf(PropertyValue.REASON to reason)
        )
    }
}
