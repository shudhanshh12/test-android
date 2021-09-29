package `in`.okcredit

import android.os.StrictMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import leakcanary.DefaultOnHeapAnalyzedListener
import leakcanary.LeakCanary
import shark.HeapAnalysisFailure
import shark.HeapAnalysisSuccess
import shark.Leak
import shark.LeakTrace
import tech.okcredit.android.base.flipper.FlipperUtils
import tech.okcredit.android.base.utils.debug
import timber.log.Timber

class DebugApp : App() {

    companion object {
        private const val INTERCEPT_LEAK_DATA = "intercept_leak_data"
    }

    override fun onCreate() {
        debug {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build()
            )
        }
        super.onCreate()
        FlipperUtils.initFlipper(this)
        GlobalScope.launch {
            startLeakCanaryForDebugBuild()
        }
        Timber.i("debug app initialized")
    }

    private fun startLeakCanaryForDebugBuild() {
        // Added feature "intercept_leak_data" in case want to intercept data to leak app please enable it
        ab.get().isFeatureEnabled(INTERCEPT_LEAK_DATA).subscribe {
            if (!it) {
                LeakCanary.config = LeakCanary.config.copy(
                    onHeapAnalyzedListener = { heapAnalysis ->
                        when (heapAnalysis) {
                            is HeapAnalysisSuccess -> {

                                val allLeakTraces = heapAnalysis
                                    .allLeaks
                                    .toList()
                                    .flatMap { leak: Leak ->
                                        leak.leakTraces.map { leakTrace: LeakTrace -> leak to leakTrace }
                                    }

                                allLeakTraces.forEach { (leak, leakTrace) ->
                                    if (leakTrace != null && !leakTrace.referencePath.isNullOrEmpty()) {
                                        val lastReferencePath = leakTrace.referencePath.last()
                                        appAnalytics.get().trackLeakCanaryData(
                                            leakTrace.leakingObject.className,
                                            "${lastReferencePath.originObject.className},${lastReferencePath.referenceName}",
                                            "${lastReferencePath.originObject.retainedHeapByteSize}",
                                            "${lastReferencePath.originObject.retainedObjectCount}"
                                        )
                                    }
                                }
                            }
                            is HeapAnalysisFailure -> {
                                appAnalytics.get().trackLeakCanaryAnalysisFailed()
                            }
                        }
                    }
                )
            } else {
                LeakCanary.config =
                    LeakCanary.config.copy(onHeapAnalyzedListener = DefaultOnHeapAnalyzedListener.create())
            }
        }
    }
}
