package merchant.okcredit.gamification.ipl.game.data.server.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LeaderBoardResponse(
    @SerializedName("merchant_score")
    val merchantScore: MerchantScore,
    @SerializedName("top_items")
    val topItems: List<MerchantScore>,
    @SerializedName("key")
    val key: String = "",
    @SerializedName("next")
    val next: Boolean
) {
    fun hasMoreData() = next
}

@Keep
data class MerchantScore(
    @SerializedName("merchant_id")
    val merchantId: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("points")
    val points: Int = 0,
    @SerializedName("rank")
    val rank: Int = -1,
    @SerializedName("money_earned")
    val moneyEarned: Float?,
    @SerializedName("profile_pic")
    val profilePic: String?,
    @SerializedName("position_type")
    val prizeType: Int
) {
    fun hideScore() = rank == -1

    fun isGoldPrize(): Boolean {
        return prizeType == PrizeType.GOLD_COIN_PRIZE.value
    }

    fun is10KMoneyPrize(): Boolean {
        return prizeType == PrizeType.MONEY_10K_PRIZE.value
    }

    fun is5KMoneyPrize(): Boolean {
        return prizeType == PrizeType.MONEY_5K_PRIZE.value
    }

    fun is2_5KMoneyPrize(): Boolean {
        return prizeType == PrizeType.MONEY_2_5K_PRIZE.value
    }

    fun is1_5KMoneyPrize(): Boolean {
        return prizeType == PrizeType.MONEY_1_5K_PRIZE.value
    }

    fun is1KMoneyPrize(): Boolean {
        return prizeType == PrizeType.MONEY_1K_PRIZE.value
    }

    fun isJerseyPrize(): Boolean {
        return prizeType == PrizeType.JERSEY_PRIZE.value
    }

    fun isMiniBatPrize(): Boolean {
        return prizeType == PrizeType.MINI_BAT_PRIZE.value
    }

    fun getPrizeType(): PrizeType {
        return PrizeType.fromPrize(prizeType)
    }
}

enum class PrizeType(var value: Int) {
    INVALID_PRIZE(0),
    GOLD_COIN_PRIZE(1),
    MONEY_10K_PRIZE(2),
    MONEY_5K_PRIZE(3),
    MONEY_2_5K_PRIZE(4),
    MONEY_1_5K_PRIZE(5),
    MONEY_1K_PRIZE(6),
    JERSEY_PRIZE(7),
    MINI_BAT_PRIZE(8);

    companion object {
        val map = values().associateBy(PrizeType::value)
        fun fromPrize(prize: Int) = map[prize] ?: INVALID_PRIZE
    }
}
