package `in`.okcredit.shared.utils

import android.app.ActivityManager
import android.content.res.ColorStateList
import android.net.Uri
import android.text.TextUtils
import android.util.Patterns
import com.google.common.base.Converter
import com.instacart.library.truetime.TrueTime
import com.instacart.library.truetime.TrueTimeRx
import org.joda.time.DateTime
import tech.okcredit.base.exceptions.ExceptionUtils.Companion.logException
import timber.log.Timber
import java.util.*

object CommonUtils {

    fun parseUpiVpaFromURl(lastResult: String): String? {
        if (lastResult.startsWith("upi://pay")) {
            val uri = Uri.parse(lastResult)
            val params = uri.queryParameterNames
            if (params.contains("pa")) {
                val value = uri.getQueryParameters("pa")
                if (value != null && value.size > 0) {
                    val upiVpa = value[0]
                    return upiVpa
                } else {
                    return null
                }
            } else {
                return null
            }
        } else {
            return null
        }
    }

    fun currentDateTime(): DateTime {
        var now: DateTime
        try {
            now = if (TrueTime.isInitialized()) {
                DateTime(TrueTimeRx.now())
            } else {
                DateTime.now()
            }
        } catch (e: Exception) {
            now = DateTime.now()
            Timber.i("TrueTime failed DeviceTime=%s, ServerTime=%s", DateTime.now(), now)
            logException("Error: TrueTime currentDateTime", e)
        }
        return now
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun normalizeEmail(email: String?): String {
        var email: String? = email ?: return ""
        email = email!!.trim { it <= ' ' }.replace(" ", "")
        return email.toLowerCase()
    }

    fun colorStateListOf(vararg mapping: Pair<IntArray, Int>): ColorStateList {
        val (states, colors) = mapping.unzip()
        return ColorStateList(states.toTypedArray(), colors.toIntArray())
    }

    fun <A, B> mapList(
        aList: List<A>,
        mapper: Converter<A, B>
    ): List<B> {
        val bList: ArrayList<B> = ArrayList(aList.size)
        for (a in aList) {
            bList.add(mapper.convert(a)!!)
        }
        return bList
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
