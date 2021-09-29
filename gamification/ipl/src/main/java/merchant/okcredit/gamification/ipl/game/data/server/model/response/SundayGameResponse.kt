package merchant.okcredit.gamification.ipl.game.data.server.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Keep
data class SundayGameResponse(
    @SerializedName("expiry_time")
    val expiryTime: Long,
    @SerializedName("merchant_stats")
    val merchantStats: MerchantStats,
    @SerializedName("threshold")
    val threshold: Threshold,
    @SerializedName("more_play")
    val morePlay: Boolean,
    @SerializedName("youtube_links")
    val youtubeLinks: List<YoutubeLinks>
) {
    fun isQualifiedForDraw(): Boolean {
        return merchantStats.boosterCount >= threshold.boosterCount && merchantStats.points >= threshold.points
    }

    fun getBoosterStatus(): SundayGameModel.BoosterSate {
        return when {
            merchantStats.boosterCount == 0 -> SundayGameModel.BoosterSate.NO_BOOSTER_DONE
            merchantStats.boosterCount > 0 && merchantStats.boosterCount < threshold.boosterCount -> SundayGameModel.BoosterSate.BOOSTER_IN_PROGRESS
            else -> SundayGameModel.BoosterSate.BOOSTER_COMPLETED
        }
    }

    fun isRunsCompleted(): Boolean {
        return merchantStats.points >= threshold.points
    }

    fun pendingPoints() = threshold.points - merchantStats.points

    fun points() = merchantStats.points

    fun dayDifference(): Int {
        return TimeUnit.MILLISECONDS.toDays(abs((expiryTime * 1000) - System.currentTimeMillis())).toInt()
    }
}

@Keep
data class MerchantStats(
    @SerializedName("booster_count")
    val boosterCount: Int,
    @SerializedName("points")
    val points: Int
)

@Keep
data class Threshold(
    @SerializedName("booster_count")
    val boosterCount: Int,
    @SerializedName("points")
    val points: Int
)
