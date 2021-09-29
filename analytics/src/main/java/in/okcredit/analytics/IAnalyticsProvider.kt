package `in`.okcredit.analytics

interface IAnalyticsProvider {

    fun setIdentity(id: String, isSignup: Boolean)

    fun setUserProperty(properties: Map<String, Any>)

    fun registerSuperProperties(properties: Map<String, Any>)

    fun setSuperProperties(properties: Map<String, Any>)

    fun trackEvents(eventName: String, properties: Map<String, Any>? = null)

    fun flushEvents()

    fun trackEngineeringMetricEvents(eventName: String, properties: Map<String, Any>?)

    fun incrementTransactionCountSuperProperty()

    fun incrementCustomerCountSuperProperty()

    fun clearIdentity()

    fun logBreadcrumb(log: String, properties: Map<String, Any>? = null)

    fun trackObjectViewed(`object`: String, properties: Map<String, Any>? = null)

    fun trackObjectInteracted(`object`: String, interactionType: InteractionType, properties: Map<String, Any>? = null)

    fun trackObjectError(`object`: String, errorMessage: String, properties: Map<String, Any>? = null)
}
