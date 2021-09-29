package merchant.android.okstream.sdk.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// TODO:: convert other snack case to camel case later

@Keep
@JsonClass(generateAdapter = true)
data class ActivityPublishEvent(
    val id: String,
    val actor: String,
    val meta: Meta,
    val user: String,
    val action: String,
    val timestamp: Long,
    val receiver: String? = "",
    val payload: Any? = null,
    val device: Device,

)

@Keep
@JsonClass(generateAdapter = true)
data class Device(
    val id: String,
    val aaid: String? = null,
    val os: String? = null,
    val version: String? = null,
    val version_code: String? = null,
    val lang: String? = null,
    @Json(name = "app_version")
    val appVersion: String? = null,
    val manufacturer: String? = null,
    val brand: String? = null,
    val bluetooth: BlueTooth? = null,
    val screen: Screen? = null,
    @Json(name = "google_play_services")
    val googlePlayServices: Any? = null,
    val android_lib: String? = null,
    val radio: String? = null,
    val place: Place? = null,
    val network: Network? = null,
)

@Keep
@JsonClass(generateAdapter = true)
data class Location(
    val lat: Float,
    val lon: Float,
)

@Keep
@JsonClass(generateAdapter = true)
data class Place(
    val country: String,
    val city: String,
    val loc: Location? = null,
)

@Keep
@JsonClass(generateAdapter = true)
data class BlueTooth(
    val enable: Boolean,
    val version: String,
)

@Keep
@JsonClass(generateAdapter = true)
data class Screen(
    val dpi: Int,
    val height: Int,
    val width: Int,
)

@Keep
@JsonClass(generateAdapter = true)
data class Network(
    val type: Type,
    val carrier: String,
    val signal_strength: String,
)

enum class Type(val type: Int) {
    WIFI(0), MOBILE(1),
}

@Keep
@JsonClass(generateAdapter = true)
data class Meta(val x_request_id: String)

@Keep
@JsonClass(generateAdapter = true)
data class AppSession(
    val id: String,
    val actor: String,
    val user: String,
    val deviceId: String,
    @Json(name = "start_time")
    val startTime: Long,
)
