package merchant.okcredit.gamification.ipl.game.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MerchantRequest(
    @SerializedName("merchant_id")
    val merchantId: String
)
