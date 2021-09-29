package `in`.okcredit.merchant.core.model.bulk_reminder

import `in`.okcredit.merchant.core.common.Timestamp
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.joda.time.DateTime

data class BackendLastReminderSendTime(
    val customerId: String,
    val lastReminderSendTime: DateTime?,
)

data class CoreLastReminderSendTime(
    val customerId: String,
    val lastReminderSendTime: Timestamp?,
)

@Keep
@JsonClass(generateAdapter = true)
data class LastReminderSendTime(
    @Json(name = "customer_id")
    val customerId: String,
    @Json(name = "last_reminder_sent")
    val lastReminderSendTime: Long = 0,
)

fun List<BackendLastReminderSendTime>.convertBackendToLastReminderSendTime() = this.map {
    LastReminderSendTime(
        customerId = it.customerId,
        lastReminderSendTime = it.lastReminderSendTime?.millis ?: 0
    )
}

fun List<CoreLastReminderSendTime>.toLastReminderSendTime() = this.map {
    LastReminderSendTime(
        customerId = it.customerId,
        lastReminderSendTime = (it.lastReminderSendTime?.seconds ?: 0) * 1000L
    )
}
