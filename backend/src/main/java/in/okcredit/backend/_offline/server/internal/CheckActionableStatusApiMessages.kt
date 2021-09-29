package `in`.okcredit.backend._offline.server.internal

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class CheckActionableStatusRequest(
    @SerializedName("device_id")
    val deviceId: String
)

@Keep
data class CheckActionableStatusResponse(
    val action: Int? = null,
    @SerializedName("action_id")
    val actionId: String? = null,
    @SerializedName("start_time_ms")
    val startTime: Long? = null,
    @SerializedName("end_time_ms")
    val endTime: Long? = null
)
