package `in`.okcredit.communication_inappnotification.remote

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class GetInAppNotificationsRequest(
    @Json(name = "device_id")
    val deviceId: String,
    @Json(name = "lang")
    val lang: String,
    @Json(name = "app_version")
    val appBuildNumber: String
)

@Keep
@JsonClass(generateAdapter = true)
data class GetInAppNotificationsResponse(
    @Json(name = "in_app_notifications")
    val inAppNotifications: List<Notification>
)

@Keep
@JsonClass(generateAdapter = true)
data class Notification(
    val id: String,
    val name: String,
    val source: String,
    @Json(name = "min_app_version")
    val minAppVersion: Int,
    @Json(name = "max_app_version")
    val maxAppVersion: Int,
    val version: String,
    @Json(name = "expiry_time")
    val expiry_time: Long, // epoch in seconds
    @Json(name = "notifications")
    val notificationData: List<NotificationData>
)

@Keep
@JsonClass(generateAdapter = true)
data class NotificationData(
    @Json(name = "screen_name")
    val screenName: String,
    val delay: Int?,
    val priority: Int?,
    val kind: String,
    val title: String?,
    val subtitle: String?,
    @Json(name = "target_id_type")
    val targetIdType: String?,
    @Json(name = "target_id")
    val targetId: String?,
    @Json(name = "target_index")
    val targetIndex: Int?,
    @Json(name = "arrow_position")
    val arrowPosition: Float?,
    @Json(name = "arrow_orientation")
    val arrowOrientation: String?,
    val radius: Int?,
    val padding: Int?,
    val template: String?,
    @Json(name = "image_height")
    val imageHeight: Int?,
    @Json(name = "image_width")
    val imageWidth: Int?,
    @Json(name = "image_url")
    val imageUrl: String?,
    @Json(name = "primary_btn")
    val primaryButton: ActionButton?,
    @Json(name = "secondary_btn")
    val secondaryButton: ActionButton?,
    @Json(name = "tertiary_btn")
    val tertiaryButton: ActionButton?
)

@Keep
@JsonClass(generateAdapter = true)
data class ActionButton(
    val text: String,
    @Json(name = "icon")
    val iconUrl: String?,
    @Json(name = "event_handlers")
    val eventHandlers: ClickHandler?
)

@Keep
@JsonClass(generateAdapter = true)
data class ClickHandler(
    @Json(name = "click")
    val clickHandlers: Set<Action>?
)

@Keep
@JsonClass(generateAdapter = true)
data class Action(
    val action: String,
    val url: String?,
    val event: String?,
    val properties: Map<String, String>?
)

@Keep
@JsonClass(generateAdapter = true)
data class AckNotificationsRequest(
    @Json(name = "notification_ids")
    val ids: List<String>,
    @Json(name = "deviceId")
    val device_id: String
)

@Keep
@JsonClass(generateAdapter = true)
data class AckNotificationsResponse(
    val code: Int? = null,
    val error: String? = null,
    val acknowledged: Boolean = false
)
