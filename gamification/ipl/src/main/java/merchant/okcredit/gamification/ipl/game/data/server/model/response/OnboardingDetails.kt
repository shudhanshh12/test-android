package merchant.okcredit.gamification.ipl.game.data.server.model.response

import com.google.gson.annotations.SerializedName

data class OnboardingDetails(
    @SerializedName("select_team_card")
    val teams: SelectTeamCard,
    @SerializedName("points_formulae")
    val pointsFormulae: PointsFormulae,
    @SerializedName("select_batsman_card")
    val batsmen: SelectPlayersCard,
    @SerializedName("select_bowlers_card")
    val bowlers: SelectPlayersCard,
    @SerializedName("expiry_time")
    val expiryTime: Long = 0,
    @SerializedName("start_time")
    val startTime: Long = 0,
    @SerializedName("booster_start_time")
    val boosterStartTime: Long?,
    @SerializedName("youtube_links")
    val youtubeLinks: List<YoutubeLinks>
) {

    fun isQualified() = teams.chosenTeam != null &&
        batsmen.chosenPlayer != null &&
        bowlers.chosenPlayer != null
}
