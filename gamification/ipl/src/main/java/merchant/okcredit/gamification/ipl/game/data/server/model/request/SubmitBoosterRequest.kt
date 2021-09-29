package merchant.okcredit.gamification.ipl.game.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SubmitBoosterRequest(
    @SerializedName("choice")
    val choice: String,
    @SerializedName("match_id")
    val matchId: String,
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("question_id")
    val questionId: String,
)
