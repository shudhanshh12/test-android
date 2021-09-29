package `in`.okcredit.shared.performance.app_startup

import android.app.ActivityManager
import android.os.Process
import android.os.SystemClock
import android.system.Os
import android.system.OsConstants
import java.io.BufferedReader
import java.io.FileReader

object AppStartUpMeasurementUtils {

    object Key {
        const val START_UP = "START_UP"
        const val PROCESS_FORK_TO_CONTENT_PROVIDER = "PROCESS_FORK_TO_CONTENT_PROVIDER"
        const val PROCESS_START_TO_CONTENT_PROVIDER = "PROCESS_START_TO_CONTENT_PROVIDER"
        const val CONTENT_PROVIDER_TO_APP_START = "CONTENT_PROVIDER_TO_APP_START"
        const val APP_ON_CREATE_TIME = "APP_ON_CREATE_TIME"
        const val APP_ON_CREATE_END_TO_FIRST_DRAW = "APP_ON_CREATE_END_TO_FIRST_DRAW"
        const val IS_FOREGROUND = "IS_FOREGROUND"
        const val DAGGER_GRAPH_CREATE_DURATION = "DAGGER_GRAPH_CREATE_DURATION"
    }

    inline fun measureTimeDiffInMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block.invoke()
        return System.currentTimeMillis() - start
    }

    fun isForegroundProcess(): Boolean {
        val processInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(processInfo)
        return processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    fun getProcessForkTime(): Long {
        val forkRealtime = readProcessForkRealtimeMillis()
        val nowRealtime = SystemClock.elapsedRealtime()
        val nowUptime = SystemClock.uptimeMillis()
        val elapsedRealtime = nowRealtime - forkRealtime

        return nowUptime - elapsedRealtime
    }

    private fun readProcessForkRealtimeMillis(): Long {
        val myPid = Process.myPid()
        val ticksAtProcessStart = readProcessStartTicks(myPid)
        val ticksPerSecond = Os.sysconf(OsConstants._SC_CLK_TCK)
        return ticksAtProcessStart * 1000 / ticksPerSecond
    }

    private fun readProcessStartTicks(pid: Int): Long {
        val path = "/proc/$pid/stat"
        val stat = BufferedReader(FileReader(path)).use { reader ->
            reader.readLine()
        }
        val fields = stat.substringAfter(") ")
            .split(' ')
        return fields[19].toLong()
    }
}
