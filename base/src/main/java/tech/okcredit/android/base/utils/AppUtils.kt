package tech.okcredit.android.base.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import tech.okcredit.android.base.AppConfig
import tech.okcredit.android.base.BuildConfig

object AppUtils {

    fun getProcessName(context: Context): String? {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val processInfo = manager?.runningAppProcesses?.firstOrNull {
            it.pid == Process.myPid()
        }
        return processInfo?.processName
    }

    fun isAppForegrounded(): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return (
            appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
            )
    }
}

inline fun debug(block: () -> Unit) {
    if (BuildConfig.DEBUG) {
        block()
    }
}

inline fun release(block: () -> Unit) {
    if (!BuildConfig.DEBUG) {
        block()
    }
}

inline fun staging(block: () -> Unit) {
    if (BuildConfig.FLAVOR == AppConfig.FLAVOR_SERVER_STAGING) {
        block()
    }
}
