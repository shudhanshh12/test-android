package tech.okcredit.contacts.server

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UploadContactRequest(
    @SerializedName("device_id") val device_id: String,
    @SerializedName("contacts") val contacts: List<Contact>,
    @SerializedName("last_batch") val last_batch: Boolean
)

@Keep
data class OkCreditContactResponse(
    @SerializedName("okc_name") val okc_name: String,
    @SerializedName("okc_number") val okc_number: String
)

@Keep
data class Contact(
    @SerializedName("phonebookId") val phonebookId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("picUri") val picUri: String?,
    @SerializedName("found") val found: Boolean?,
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("type") val type: Int?
)
