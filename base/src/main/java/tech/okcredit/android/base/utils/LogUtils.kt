package tech.okcredit.android.base.utils

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bugfender.sdk.Bugfender
import tech.okcredit.android.base.BuildConfig
import tech.okcredit.secure_keys.KeyProvider
import timber.log.Timber
import java.util.*

/****************************************************************
 * Log utils
 ****************************************************************/

fun OneTimeWorkRequest.enableWorkerLogging(): OneTimeWorkRequest {
    LogUtils.logWorkInfo(this.id)
    return this
}

object LogUtils {
    fun enableWorkerLogging(workRequest: WorkRequest) {
        logWorkInfo(workRequest.id)
    }

    fun logWorkInfo(id: UUID) {
        if (BuildConfig.DEBUG) {
            val mainHandler = Handler(Looper.getMainLooper())

            val myRunnable =
                Runnable {
                    WorkManager.getInstance().getWorkInfoByIdLiveData(id).observeForever { it ->
                        it?.let { data ->
                            Timber.v(
                                "<<<<<Worker State:%s tags=%s runAttemptCount=%d",
                                data.state.toString(),
                                data.tags.toString(),
                                data.runAttemptCount
                            )
                        }
                    }
                }
            mainHandler.post(myRunnable)
        }
    }

    fun startRemoteLogging(context: Context, merchantId: String, txns: String, customers: String) {
        val mainHandler = Handler(Looper.getMainLooper())

        val myRunnable = Runnable {

            Bugfender.init(context, KeyProvider.getBugfenderToken(), BuildConfig.DEBUG)
            Bugfender.enableCrashReporting()
            Bugfender.enableLogcatLogging()
            Bugfender.enableUIEventLogging(context as Application)

            if (merchantId.isNotEmpty())
                Bugfender.setDeviceString("user.merchant_id", merchantId)
            if (txns.isNotEmpty())
                Bugfender.setDeviceString("TRANSACTIONS", txns)
            if (customers.isNotEmpty())
                Bugfender.setDeviceString("CUSTOMERS", customers)

            Timber.plant(RemoteLoggingTree())
        }
        mainHandler.post(myRunnable)
    }
}

class RemoteLoggingTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, "log.$tag", message, t)

        when (priority) {
            Log.DEBUG -> {
                Bugfender.d(tag, message)
            }
            Log.INFO -> {
                Bugfender.i(tag, message)
            }
            Log.WARN -> {
                Bugfender.w(tag, message)
            }
            Log.ERROR -> {
                Bugfender.e(tag, message)
            }
            Log.ASSERT -> {
                Bugfender.e(tag, message)
            }
        }

        if (t != null) {
            Bugfender.e("Stacktrace:", t.getStringStackTrace())
        }
    }
}
