package merchant.android.okstream.contract.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class OkStreamNotification(
    val id: String,
    val name: String,
    val labels: Map<String, String>? = null,
    val type: Int,
    val version: Int,
    val visible: Visible? = null,
)

enum class Type(type: Int) {
    SILENT(0), VISIBLE(1)
}

@Keep
@JsonClass(generateAdapter = true)
data class Visible(
    val title: String,
    val content: String,
    @Json(name = "primary_action")
    val primaryAction: String,
    @Json(name = "expire_time")
    val expireTime: Long,
    val priority: Int,
)
