package `in`.okcredit.shared.performance

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.shared.performance.PerformanceTracker.Event.appStartUp
import `in`.okcredit.shared.performance.PerformanceTracker.Event.layoutPerformance
import `in`.okcredit.shared.performance.app_startup.AppStartUpMeasurementUtils
import `in`.okcredit.shared.performance.app_startup.StartUpMeasurementDataObject
import `in`.okcredit.shared.performance.layout_perf.LayoutPerformanceData
import dagger.Lazy
import org.jetbrains.annotations.NonNls
import javax.inject.Inject

class PerformanceTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {

    object Event {
        const val layoutPerformance = "Layout Performance"
        const val appStartUp = "App StartUp"
        const val DEVICE_MEMORY_DATA = "Device Memory Data"
    }

    object Key {
        const val layoutName = "LAYOUT_NAME"
        const val isRecyclerview = "IS_RECYCLER_VIEW"
        const val totalOnMeasureCountBeforeFirstOnLayout = "TOTAL_ON_MEASURE_COUNT_BEFORE_FIRST_ON_LAYOUT"
        const val totalOnMeasureDurationBeforeFirstOnLayout = "TOTAL_ON_MEASURE_DURATION_BEFORE_FIRST_ON_LAYOUT"

        const val totalOnMeasureCountBeforeSecondOnLayout = "TOTAL_ON_MEASURE_COUNT_BEFORE_SECOND_ON_LAYOUT"
        const val totalOnMeasureDurationBeforeSecondOnLayout = "TOTAL_ON_MEASURE_DURATION_BEFORE_SECOND_ON_LAYOUT"

        const val totalOnMeasureCount = "TOTAL_ON_MEASURE_COUNT"
        const val totalOnMeasureDuration = "TOTAL_ON_MEASURE_DURATION"
        const val maxOfOnMeasure = "MAX_OF_ON_MEASURE"

        const val totalOnLayoutCount = "TOTAL_ON_LAYOUT_COUNT"
        const val totalOnLayoutDuration = "TOTAL_ON_LAYOUT_DURATION"

        const val totalOnDrawCount = "TOTAL_ON_DRAW_COUNT"
        const val totalOnDrawDuration = "TOTAL_ON_DRAW_DURATION_IN_NANO"

        const val totalSizeChangedCount = "TOTAL_SIZE_CHANGED_COUNT"
    }

    @NonNls
    fun trackEvents(
        eventName: String,
        propertiesMap: PropertiesMap? = null
    ) {
        val properties = propertiesMap?.map() ?: mutableMapOf()
        analyticsProvider.get().trackEngineeringMetricEvents(eventName, properties)
    }

    fun trackLayoutPerformance(layoutPerformanceData: LayoutPerformanceData) {
        val properties = HashMap<String, Any>().apply {
            this[Key.layoutName] = layoutPerformanceData.layoutName
            this[Key.isRecyclerview] = layoutPerformanceData.isRecyclerView
            this[Key.totalOnMeasureCountBeforeFirstOnLayout] =
                layoutPerformanceData.totalOnMeasureCountBeforeFirstOnLayout
            this[Key.totalOnMeasureDurationBeforeFirstOnLayout] =
                layoutPerformanceData.totalOnMeasureDurationBeforeFirstOnLayout

            this[Key.totalOnMeasureCountBeforeSecondOnLayout] =
                layoutPerformanceData.totalOnMeasureCountBeforeSecondOnLayout
            this[Key.totalOnMeasureDurationBeforeSecondOnLayout] =
                layoutPerformanceData.totalOnMeasureDurationBeforeSecondOnLayout

            this[Key.totalOnMeasureCount] = layoutPerformanceData.totalOnMeasureCount
            this[Key.totalOnMeasureDuration] = layoutPerformanceData.totalOnMeasureDuration
            this[Key.maxOfOnMeasure] = layoutPerformanceData.maxOfOnMeasure

            this[Key.totalOnLayoutCount] = layoutPerformanceData.totalOnLayoutCount
            this[Key.totalOnLayoutDuration] = layoutPerformanceData.totalOnLayoutDuration

            this[Key.totalOnDrawCount] = layoutPerformanceData.totalOnDrawCount
            this[Key.totalOnDrawDuration] = layoutPerformanceData.totalOnDrawDuration

            this[Key.totalSizeChangedCount] = layoutPerformanceData.totalSizeChangedCount
        }
        analyticsProvider.get().trackEngineeringMetricEvents(layoutPerformance, properties)
    }

    fun trackAppStartUp() {
        val properties = HashMap<String, Any>().apply {
            this[AppStartUpMeasurementUtils.Key.START_UP] =
                StartUpMeasurementDataObject.firstDrawTime - StartUpMeasurementDataObject.processForkTime

            this[AppStartUpMeasurementUtils.Key.PROCESS_FORK_TO_CONTENT_PROVIDER] =
                StartUpMeasurementDataObject.contentProviderStartedTime - StartUpMeasurementDataObject.processForkTime

            this[AppStartUpMeasurementUtils.Key.CONTENT_PROVIDER_TO_APP_START] =
                StartUpMeasurementDataObject.appOnCreateTime - StartUpMeasurementDataObject.contentProviderStartedTime

            this[AppStartUpMeasurementUtils.Key.APP_ON_CREATE_TIME] =
                StartUpMeasurementDataObject.appOnCreateEndTime - StartUpMeasurementDataObject.appOnCreateTime

            this[AppStartUpMeasurementUtils.Key.APP_ON_CREATE_END_TO_FIRST_DRAW] =
                StartUpMeasurementDataObject.firstDrawTime - StartUpMeasurementDataObject.appOnCreateEndTime

            this[AppStartUpMeasurementUtils.Key.DAGGER_GRAPH_CREATE_DURATION] =
                StartUpMeasurementDataObject.daggerGraphCreationTime
        }

        if (StartUpMeasurementDataObject.processStartTime != 0L) {
            properties[AppStartUpMeasurementUtils.Key.PROCESS_START_TO_CONTENT_PROVIDER] =
                StartUpMeasurementDataObject.contentProviderStartedTime - StartUpMeasurementDataObject.processStartTime
        }

        StartUpMeasurementDataObject.isForeground?.let {
            properties[AppStartUpMeasurementUtils.Key.IS_FOREGROUND] = it
        }
        analyticsProvider.get().trackEngineeringMetricEvents(appStartUp, properties)
    }
}
