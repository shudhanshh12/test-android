package `in`.okcredit.shared.performance.memory

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.shared.performance.PerformanceTracker.Event.DEVICE_MEMORY_DATA
import dagger.Lazy
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class TrackMemoryData @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
    private val getDeviceMemoryData: Lazy<GetDeviceMemoryData>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
) {

    suspend fun execute(screen: String) {
        return withContext(dispatcherProvider.get().io()) {
            try {
                val res = getDeviceMemoryData.get().execute()
                analyticsProvider.get()
                    .trackEngineeringMetricEvents(
                        DEVICE_MEMORY_DATA,
                        mapOf(
                            "Max Heap Memory" to res.heapMemoryResponse.maxHeapMemory,
                            "Total Allocated Heap Memory" to res.heapMemoryResponse.totalAllocatedHeapMemory,
                            "Free Allocated Heap Memory" to res.heapMemoryResponse.freeAllocatedHeapMemory,
                            "Used Heap Memory" to res.heapMemoryResponse.usedHeapMemory,
                            "Percentage Allocated Heap Memory" to res.heapMemoryResponse.percentageOfHeapAllocated,
                            "Percentage Used Heap Memory" to res.heapMemoryResponse.percentageOfHeapUsed,

                            "Total RAM" to res.ramMemoryData.totalMemory,
                            "Used RAM" to res.ramMemoryData.usedMemory,
                            "Used RAM Percentage" to res.ramMemoryData.usedMemoryPercentage,
                            "Free RAM" to res.ramMemoryData.freeMemory,
                            "Free RAM Percentage" to res.ramMemoryData.freeMemoryPercentage,
                            "Free RAM With Cache" to res.ramMemoryData.availableMemory,
                            "Cached RAM" to res.ramMemoryData.cachedMemory,
                            "Cache RAM Percentage" to res.ramMemoryData.cacheMemoryPercentage,
                            "RAM Threshold" to res.ramMemoryData.memoryThreshold,
                            "RAM Threshold Percentage" to res.ramMemoryData.thresholdMemoryPercentage,

                            "Is Device On Low Memory" to res.ramMemoryData.isDeviceOnLowMemory,
                            "Screen" to screen
                        )
                    )
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }
    }
}
