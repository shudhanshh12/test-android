package tech.okcredit.android.base.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.joda.time.DateTime
import tech.okcredit.android.base.datetime.fromEpoch
import tech.okcredit.android.base.datetime.toEpoch

class DateTimeAdapter {
    @FromJson
    fun dateTimeFromString(epoch: String?): DateTime? {
        return try {
            fromEpoch(epoch?.toLong())
        } catch (e: Exception) {
            null
        }
    }

    @ToJson
    fun dateTimeToString(dateTime: DateTime?): String {
        return toEpoch(dateTime).toString()
    }
}
