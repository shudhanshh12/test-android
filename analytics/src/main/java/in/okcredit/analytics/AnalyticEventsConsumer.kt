package `in`.okcredit.analytics

interface AnalyticEventsConsumer {

    fun setIdentity(id: String, isSignup: Boolean)

    fun setUserProperty(properties: Map<String, Any>)

    fun registerSuperProperties(properties: Map<String, Any>)

    fun setSuperProperties(properties: Map<String, Any>)

    fun trackEvents(eventName: String, properties: Map<String, Any>?)

    fun flushEvents()

    fun incrementTransactionCountSuperProperty()

    fun incrementCustomerCountSuperProperty()

    fun clearIdentity()
}
