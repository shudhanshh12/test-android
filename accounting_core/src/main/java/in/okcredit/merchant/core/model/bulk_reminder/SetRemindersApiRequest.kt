package `in`.okcredit.merchant.core.model.bulk_reminder

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class SetRemindersApiRequest(
    @Json(name = "reminders")
    val reminders: List<LastReminderSendTime>,
)
