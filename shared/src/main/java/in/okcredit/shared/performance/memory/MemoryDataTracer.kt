package `in`.okcredit.shared.performance.memory

import `in`.okcredit.shared.base.BaseScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.crashlytics.RecordException

class MemoryDataTracer constructor(
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val trackMemoryData: Lazy<TrackMemoryData>,
    private val label: String = "Screen",
) : LifecycleObserver {

    private var memoryTrackingJob: Job? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        val samplingMemoryInstrumentation =
            firebaseRemoteConfig.get().getLong(BaseScreen.DEVICE_MEMORY_TRACKING_SAMPLING).toInt()
        if ((0..100).random() >= samplingMemoryInstrumentation) {
            return
        }

        memoryTrackingJob = lifecycleScope.launch(
            CoroutineExceptionHandler { _, t ->
                RecordException.recordException(t)
            }
        ) {
            delay(2000) // Adding 2 Sec Delay By Assuming All Tasks for the screen will be finished in this duration
            trackMemoryData.get().execute(label)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() = lifecycleScope.launch(Dispatchers.Default) {
        memoryTrackingJob?.cancel()
    }
}
