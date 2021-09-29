package `in`.okcredit.shared.performance.layout_perf

import `in`.okcredit.shared.performance.PerformanceTracker
import dagger.Lazy
import timber.log.Timber

data class LayoutPerformanceData(
    var shouldLogLayoutPerf: Boolean = false,
    var isRecyclerView: Boolean = false,
    var layoutName: String = "",

    var totalOnMeasureCountBeforeFirstOnLayout: Int = 0,
    var totalOnMeasureDurationBeforeFirstOnLayout: Long = 0L,
    var isFirstOnLayoutCalled: Boolean = false,

    var totalOnMeasureCountBeforeSecondOnLayout: Int = 0,
    var totalOnMeasureDurationBeforeSecondOnLayout: Long = 0L,
    var isSecondOnLayoutCalled: Boolean = false,

    var totalOnMeasureCount: Int = 0,
    var totalOnMeasureDuration: Long = 0L,
    var maxOfOnMeasure: Long = 0L,

    var totalOnLayoutCount: Int = 0,
    var totalOnLayoutDuration: Long = 0L,

    var totalOnDrawCount: Int = 0,
    var totalOnDrawDuration: Long = 0L,

    var totalSizeChangedCount: Int = 0,
) {

    fun onMeasureCalled(onMeasureDuration: Long) {
        if (shouldLogLayoutPerf) {
            if (onMeasureDuration > maxOfOnMeasure) {
                maxOfOnMeasure = onMeasureDuration
            }

            if (isFirstOnLayoutCalled.not()) {
                totalOnMeasureCountBeforeFirstOnLayout++
                totalOnMeasureDurationBeforeFirstOnLayout += onMeasureDuration
            } else if (isFirstOnLayoutCalled && isSecondOnLayoutCalled.not()) {
                totalOnMeasureCountBeforeSecondOnLayout++
                totalOnMeasureDurationBeforeSecondOnLayout += onMeasureDuration
            }

            totalOnMeasureCount++
            totalOnMeasureDuration += onMeasureDuration
        }
    }

    fun onLayoutCalled(onLayoutDuration: Long) {
        if (shouldLogLayoutPerf) {
            if (isFirstOnLayoutCalled) {
                isSecondOnLayoutCalled = true
            }
            isFirstOnLayoutCalled = true

            totalOnLayoutCount++
            totalOnLayoutDuration += onLayoutDuration
        }
    }

    fun onSizeChanged() {
        if (shouldLogLayoutPerf) {
            totalSizeChangedCount++
        }
    }

    fun onDrawCalled(totalOnDrawInNanoSec: Long) {
        if (shouldLogLayoutPerf) {
            totalOnDrawCount++
            totalOnDrawDuration += totalOnDrawInNanoSec
        }
    }

    fun track(layoutPerformanceTracker: Lazy<PerformanceTracker>?) {
        if (shouldLogLayoutPerf) {
            layoutPerformanceTracker?.get()?.trackLayoutPerformance(
                this
            )
            shouldLogLayoutPerf = false
        }
    }

    fun shouldTrackThisView(isRecycler: Boolean) {
        if (isRecycler) {
            isRecyclerView = true
            if ((0..999).random() < 2) { // 0.2% Sampling
                shouldLogLayoutPerf = true
            }
            return
        }

        if ((0..99).random() < 2) { // 2% Sampling
            shouldLogLayoutPerf = true
        }
    }

    inline fun logOnDraw(tag: String, block: () -> Unit) {
        if (shouldLogLayoutPerf) {
            val start = System.nanoTime()
            block.invoke()
            val duration = System.nanoTime() - start
            Timber.d("$tag onDraw $duration")
            onDrawCalled(duration)
        } else {
            block.invoke()
        }
    }

    inline fun logOnMeasure(tag: String, block: () -> Unit) {
        if (shouldLogLayoutPerf) {
            val start = System.currentTimeMillis()
            block.invoke()
            val duration = System.currentTimeMillis() - start
            Timber.d("$tag onMeasure $duration")
            onMeasureCalled(duration)
        } else {
            block.invoke()
        }
    }

    inline fun logOnLayout(tag: String, block: () -> Unit) {
        if (shouldLogLayoutPerf) {
            val start = System.currentTimeMillis()
            block.invoke()
            val duration = System.currentTimeMillis() - start
            Timber.d("$tag onLayout $duration")
            onLayoutCalled(duration)
        } else {
            block.invoke()
        }
    }

    fun logOnSizeChanged(tag: String) {
        if (shouldLogLayoutPerf) {
            Timber.d("$tag onSizeChanged")
            onSizeChanged()
        }
    }

    fun logOnDetachedFromWindow(tag: String, layoutPerformanceTracker: Lazy<PerformanceTracker>?) {
        if (shouldLogLayoutPerf) {
            Timber.d("$tag onDetachedFromWindow")
            track(layoutPerformanceTracker)
        }
    }
}
