package `in`.okcredit.fileupload._id

import android.app.ActivityManager
import android.content.Context

class GlideMemoryPerformanceCheckerHelper {

    companion object {
        private const val OPTIMUM_CORE = 4
        private const val OPTIMUM_MEMORY_MB = 124

        fun isPerformanceDevice(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val devicePerformance: Boolean

            devicePerformance =
                !activityManager.isLowRamDevice &&
                Runtime.getRuntime().availableProcessors() >= OPTIMUM_CORE &&
                activityManager.memoryClass >= OPTIMUM_MEMORY_MB

            return devicePerformance
        }
    }
}
