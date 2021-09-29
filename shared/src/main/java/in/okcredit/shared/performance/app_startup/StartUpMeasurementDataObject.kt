package `in`.okcredit.shared.performance.app_startup

import android.os.Build
import android.os.Process
import java.util.concurrent.Executors

object StartUpMeasurementDataObject {

    var traceStartUpEnabled = true

    var processForkTime = 0L
    var processStartTime = 0L
    var contentProviderStartedTime = 0L
    var appOnCreateTime = 0L
    var appOnCreateEndTime = 0L
    var firstDrawTime = 0L

    var daggerGraphCreationTime = 0L
    var isForeground: Boolean? = null

    fun setProcessInfo() {
        Executors.newCachedThreadPool().execute {
            isForeground = AppStartUpMeasurementUtils.isForegroundProcess()
            processForkTime = AppStartUpMeasurementUtils.getProcessForkTime()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                processStartTime = Process.getStartUptimeMillis()
            }
        }
    }

    // since measure time intervals using SystemClock.uptimeMillis() sometimes bind application starts then halts
    // mid way and the actual app start is much later. so adding a 30 sec limit for filtering those out.
    fun isValidAppStartUpMeasure(): Boolean {
        return processForkTime != 0L && contentProviderStartedTime != 0L && appOnCreateTime != 0L &&
            appOnCreateEndTime != 0L && firstDrawTime != 0L && firstDrawTime - processForkTime < 30_000
    }
}
