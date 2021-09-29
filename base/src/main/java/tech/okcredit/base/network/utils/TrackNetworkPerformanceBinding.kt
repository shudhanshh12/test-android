package tech.okcredit.base.network.utils

interface TrackNetworkPerformanceBinding {
    fun trackNetworkPerformance(eventName: String, properties: Map<String, Any>? = null)
}
