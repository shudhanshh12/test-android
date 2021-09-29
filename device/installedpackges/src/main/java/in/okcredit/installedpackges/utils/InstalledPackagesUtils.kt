package `in`.okcredit.installedpackges.utils

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object InstalledPackagesUtils {
    fun getDaysDiffFrmTimestamps(pastDate: Long, currentDate: Long): Long {
        return TimeUnit.DAYS.convert(currentDate.minus(pastDate), TimeUnit.MILLISECONDS)
    }

    fun getTimestampFromString(str: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(str).time
        } catch (ex: Exception) {
            System.currentTimeMillis()
        }
    }
}
