package tech.okcredit.contacts.server.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CheckedResponse(
    @SerializedName("contacts") val contacts: List<Contact>,
    @SerializedName("next") val next: Next
)

@Keep
data class Contact(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("found") val found: Boolean,
    @SerializedName("type") val type: Int,
    @SerializedName("timestamp") val timestamp: Long
)

@Keep
data class Next(
    @SerializedName("has_more") val hasMore: Boolean,
    @SerializedName("start_ts") val startTimestamp: Long,
    @SerializedName("last_id") val lastId: String?
)
