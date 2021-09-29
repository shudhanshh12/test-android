package `in`.okcredit.di.binding.network

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.shared.performance.PerformanceTracker
import dagger.Lazy
import tech.okcredit.base.network.utils.TrackNetworkPerformanceBinding
import javax.inject.Inject

class TrackerNetworkPerformanceImpl @Inject constructor(private val performanceTracker: Lazy<PerformanceTracker>) :
    TrackNetworkPerformanceBinding {
    override fun trackNetworkPerformance(eventName: String, properties: Map<String, Any>?) {
        val propertyMap = PropertiesMap.create()
        properties?.map {
            propertyMap.add(it.key, it.value)
        }
        performanceTracker.get().trackEvents(eventName, propertyMap)
    }
}
