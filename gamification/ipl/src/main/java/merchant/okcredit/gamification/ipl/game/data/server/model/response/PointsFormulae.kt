package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class PointsFormulae(
    @SerializedName("winning_points")
    val winningPoints: Int = 0,
    @SerializedName("run_multiplier")
    val runMultiplier: Int = 0,
    @SerializedName("wicket_multiplier")
    val wicketMultiplier: Int = 0,
    @SerializedName("onboarding_points")
    val onboardingPoints: Int = 0
)
