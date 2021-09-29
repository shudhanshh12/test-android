package `in`.okcredit.shared.performance.frame_rate

import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.shared.performance.PerformanceTracker
import androidx.core.app.FrameMetricsAggregator
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber

class FragmentFrameRateTracer constructor(
    private val activity: FragmentActivity,
    private val performanceTracker: PerformanceTracker,
    private val label: String = "Screen"
) : LifecycleObserver {

    private val aggregator: FrameMetricsAggregator = FrameMetricsAggregator()

    private var totalFrames = 0L
    private var slowFrames = 0L
    private var laggyFrames = 0L
    private var frozenFrames = 0L

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() = activity.lifecycleScope.launch(Dispatchers.Default) {
        try {
            aggregator.add(activity)
        } catch (e: Exception) {
            ExceptionUtils.logException(e)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() = activity.lifecycleScope.launch(Dispatchers.Default) {
        try {
            val data = aggregator.metrics ?: return@launch

            totalFrames = 0L
            slowFrames = 0L
            laggyFrames = 0L
            frozenFrames = 0L

            data[FrameMetricsAggregator.TOTAL_INDEX].let { distributions ->
                for (i in 0 until distributions.size()) {
                    val duration = distributions.keyAt(i)
                    val frameCount = distributions.valueAt(i)
                    totalFrames += frameCount
                    if (duration > 16)
                        slowFrames += frameCount
                    if (duration > 32)
                        laggyFrames += frameCount
                    if (duration > 700)
                        frozenFrames += frameCount
                }
            }
            aggregator.reset()
            val frameRateData = FrameRateData(totalFrames, slowFrames, laggyFrames, frozenFrames)
            Timber.i("$label Frame Tracer Result :\n$frameRateData")

            performanceTracker.trackEvents(
                "Frame Rate",
                PropertiesMap.create()
                    .add("Screen", label)
                    .add("Total Frame", totalFrames)
                    .add("Slow Frame", slowFrames)
                    .add("Laggy Frame", laggyFrames)
                    .add("Frozen Frame", frozenFrames)
                    .add("Slow Frame Rate", frameRateData.getSlowFrameRate())
                    .add("Laggy Frame Rate", frameRateData.getLaggyFrameRate())
                    .add("Frozen Frame Rate", frameRateData.getFrozenFrameRate())
            )
        } catch (e: Exception) {
            ExceptionUtils.logException(e)
        }
    }
}
