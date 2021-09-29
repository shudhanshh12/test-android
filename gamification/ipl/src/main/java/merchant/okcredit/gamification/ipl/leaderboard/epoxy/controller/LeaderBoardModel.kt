package merchant.okcredit.gamification.ipl.leaderboard.epoxy.controller

sealed class LeaderBoardModel {
    data class PrizeGroupHeader(val collapsed: Boolean) : LeaderBoardModel()
    data class Header(val prizeGroup: PrizeGroup) : LeaderBoardModel()

    data class Self(
        val displayImage: String?,
        val name: String,
        val totalPoints: Int,
        val moneyEarned: Float,
        val rank: Int
    ) : LeaderBoardModel()

    data class OtherMerchant(
        val displayImage: String?,
        val name: String,
        val totalPoints: Int,
        val rank: Int
    ) : LeaderBoardModel()
}
