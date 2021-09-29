package `in`.okcredit.shared.performance.app_startup

import `in`.okcredit.shared.performance.PerformanceTracker
import `in`.okcredit.shared.performance.app_startup.NextDrawListener.Companion.onNextDraw
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import dagger.Lazy
import javax.inject.Inject

class AppStartMeasureLifeCycleCallBacks @Inject constructor(
    private val performanceTracker: Lazy<PerformanceTracker>,
) : Application.ActivityLifecycleCallbacks {

    var firstDraw = false

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (!firstDraw) {
            activity?.window?.decorView?.onNextDraw {
                if (firstDraw) return@onNextDraw
                firstDraw = true
                StartUpMeasurementDataObject.firstDrawTime = SystemClock.uptimeMillis()

                if (StartUpMeasurementDataObject.isValidAppStartUpMeasure()) {
                    performanceTracker.get().trackAppStartUp()
                }
            }
        }
    }
}
