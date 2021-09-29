package merchant.okcredit.gamification.ipl.game.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LeaderBoardRequest(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("key")
    val key: String
)
