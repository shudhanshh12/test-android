package tech.okcredit.android.base.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.CancellationException
import tech.okcredit.android.base.extensions.isConnectedToInternet

abstract class BaseCoroutineWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters,
    private val workerConfig: WorkerConfig = WorkerConfig(),
) : CoroutineWorker(appContext, workerParams) {

    private var outputData: Data? = null

    override suspend fun doWork(): Result {
        val tracer = FirebasePerformance.getInstance().newTrace("TraceWorker")
        startTracerIfTrackingIsEnabled(tracer)
        return try {
            doActualWork()
            putAttributeAndStopTracerIfTrackingIsEnabled(tracer, "Success")
            successResult()
        } catch (cancellation: CancellationException) {
            putAttributeAndStopTracerIfTrackingIsEnabled(tracer, "Cancelled")
            failureResult()
        } catch (exception: Exception) {
            putAttributeAndStopTracerIfTrackingIsEnabled(tracer, "Failed")
            if (canRetry()) Result.retry() else failureResult()
        }
    }

    private fun startTracerIfTrackingIsEnabled(tracer: Trace) {
        if (workerConfig.trackingEnabled) {
            tracer.start()
            tracer.putAttribute("runAttemptCount", workerParams.runAttemptCount.toString())
            tracer.putAttribute("internet", appContext.isConnectedToInternet().toString())
            tracer.putAttribute("name", getWorkerName())
        }
    }

    private fun canRetry() = runAttemptCount < workerConfig.maxAttemptCount || workerConfig.allowUnlimitedRun

    private fun putAttributeAndStopTracerIfTrackingIsEnabled(tracer: Trace, attribute: String) {
        if (workerConfig.trackingEnabled) {
            tracer.putAttribute("Result", attribute)
            tracer.stop()
        }
    }

    private fun successResult() = outputData?.let { outputData -> Result.success(outputData) } ?: Result.success()

    private fun failureResult() = outputData?.let { outputData -> Result.failure(outputData) } ?: Result.failure()

    private fun getWorkerName(): String {
        val workerName = javaClass.canonicalName?.filter { it.isLetterOrDigit() } ?: "UnKnown"
        return if (workerName.length > 32) {
            workerName.takeLast(30)
        } else {
            workerName
        }
    }

    fun setOutputData(outputData: Data) {
        this.outputData = outputData
    }

    abstract suspend fun doActualWork()
}
