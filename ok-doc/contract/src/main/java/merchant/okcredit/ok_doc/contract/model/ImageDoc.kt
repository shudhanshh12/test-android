package merchant.okcredit.ok_doc.contract.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ImageDoc(
    @SerializedName("id")
    val id: String,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("medium")
    val medium: String
)
