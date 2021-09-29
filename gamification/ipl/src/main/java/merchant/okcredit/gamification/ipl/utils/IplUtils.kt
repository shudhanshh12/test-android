package merchant.okcredit.gamification.ipl.utils

import android.content.Context
import com.instacart.library.truetime.TrueTime
import com.instacart.library.truetime.TrueTimeRx
import merchant.okcredit.gamification.ipl.R
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

object IplUtils {
    const val IPL_WHATS_APP_HELP_NUMBER = "8296508123"

    private val simpleDateFormat = SimpleDateFormat("MMM dd | hh:mm aa", Locale.ENGLISH)

    fun getStartTime(epochTime: Long): String = simpleDateFormat.format(Date(epochTime * 1000))

    fun getEndsInTime(startDate: Date, endDate: Date, context: Context): String {
        var different = endDate.time - startDate.time
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different %= daysInMilli
        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli
        val elapsedSeconds = different / secondsInMilli

        val daysFormat: String
        val hoursFormat: String
        val minutesFormat: String
        val secondsFormat: String
        daysFormat = if (elapsedDays == 0L) {
            ""
        } else {
            context.resources.getQuantityString(R.plurals.days, elapsedDays.toInt(), elapsedDays.toString())
        }
        hoursFormat = if (elapsedHours == 0L) {
            ""
        } else {
            context.getString(R.string.hours, elapsedHours.toString())
        }

        minutesFormat = context.getString(R.string.minutes, elapsedMinutes.toString())
        secondsFormat = context.getString(R.string.seconds, elapsedSeconds.toString())

        val stringBuilder = StringBuilder().apply {
            if (daysFormat.isNotEmpty()) append(daysFormat)
            if (hoursFormat.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(hoursFormat)
            }
            if (minutesFormat.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(minutesFormat)
            }
            if (secondsFormat.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(secondsFormat)
            }
        }
        return stringBuilder.toString()
    }

    fun getCurrentDateTime(): Date {
        var currentDateTime = Date()
        if (TrueTime.isInitialized()) {
            currentDateTime = Date(TrueTimeRx.now().time)
        }
        return currentDateTime
    }

    fun hasGameExpired(expiry: Long): Boolean {
        val expiryDateTime = Date(expiry * 1000)
        return expiryDateTime.before(getCurrentDateTime())
    }

    fun isThisWeeksReward(millis: Long): Boolean {
        val today = Calendar.getInstance()
        today.timeInMillis = getCurrentDateTime().time
        val todayWeek: Int = today.get(Calendar.WEEK_OF_YEAR)
        val todayYear: Int = today.get(Calendar.YEAR)

        val rewardDate = Calendar.getInstance()
        rewardDate.timeInMillis = millis
        val rewardWeek = rewardDate[Calendar.WEEK_OF_YEAR]
        val rewardYear = rewardDate[Calendar.YEAR]

        return todayWeek == rewardWeek && todayYear == rewardYear
    }

    fun getErrorCode(throwable: Throwable): Int {
        var code = -1
        if (throwable is HttpException) {
            code = throwable.code()
        }
        return code
    }
}
