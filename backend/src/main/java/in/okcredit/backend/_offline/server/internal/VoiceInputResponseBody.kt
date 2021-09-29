package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

data class VoiceInputResponseBody(
    @SerializedName("amount")
    val amount: Int,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("method")
    val method: String? = null,
)
