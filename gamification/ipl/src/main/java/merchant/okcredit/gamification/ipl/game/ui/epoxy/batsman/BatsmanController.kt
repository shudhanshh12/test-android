package merchant.okcredit.gamification.ipl.game.ui.epoxy.batsman

import android.content.Context
import android.graphics.Color
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Player
import merchant.okcredit.gamification.ipl.game.ui.GameContract
import merchant.okcredit.gamification.ipl.game.ui.epoxy.ItemLoadMore
import merchant.okcredit.gamification.ipl.game.ui.epoxy.itemTeamName
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.getColorCompat
import javax.inject.Inject

class BatsmanController @Inject constructor(
    private val callbackSelect: Lazy<ItemBatman.BatsmanListener>,
    private val callbackLoadMore: Lazy<ItemLoadMore.LoadMoreListener>,
    context: Lazy<Context>,
) : AsyncEpoxyController() {

    var distinctPlayerList = mutableListOf<Player>()
    lateinit var state: GameContract.State
    private var homeParsedColor: Int = context.get().getColorCompat(R.color.red_lite_1)
    private var awayParsedColor: Int = context.get().getColorCompat(R.color.orange_lite_1)

    fun setData(state: GameContract.State) {
        this.state = state
        val prediction = state.prediction
        if (prediction != null) {
            val homeColor = prediction.matchPrediction.homeTeam.colorCode
            val awayColor = prediction.matchPrediction.awayTeam.colorCode

            try {
                homeParsedColor = Color.parseColor(homeColor)
                awayParsedColor = Color.parseColor(awayColor)
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }
        requestModelBuild()
    }

    override fun buildModels() {
        val players = state.onboardingDetails?.batsmen?.allPlayers ?: return

        itemTeamName {
            id("batsman_home_team_name")
            teamName(state.onboardingDetails?.teams?.homeTeam?.shortName)
            teamColor(awayParsedColor)
        }

        itemTeamName {
            id("batsman_away_team_name")
            teamName(state.onboardingDetails?.teams?.awayTeam?.shortName)
            teamColor(homeParsedColor)
        }
        val homeTeamPlayerList = state.onboardingDetails?.batsmen?.allPlayers?.filter { it.isHomeTeam() } as ArrayList
        val awayTeamPlayerList = state.onboardingDetails?.batsmen?.allPlayers?.filter { it.isAwayTeam() } as ArrayList

        val playerList = state.onboardingDetails?.batsmen?.allPlayers as MutableList

        if (distinctPlayerList.isNullOrEmpty()) {
            try {
                playerList.forEachIndexed { index, player ->
                    if (index % 2 != 0) {
                        if (awayTeamPlayerList.isEmpty().not()) {
                            playerList[index] = awayTeamPlayerList[0]
                            awayTeamPlayerList.removeAt(0)
                        }
                    } else {
                        if (homeTeamPlayerList.isEmpty().not()) {
                            playerList[index] = homeTeamPlayerList[0]
                            homeTeamPlayerList.removeAt(0)
                        }
                    }
                }
                if (homeTeamPlayerList.isNullOrEmpty().not()) {
                    playerList.addAll(homeTeamPlayerList)
                }
                val similar = playerList.groupingBy { it }.eachCount().filter { it.value > 1 }
                distinctPlayerList = playerList.distinctBy { it.id }.toMutableList()
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }

        val size = if (state.showAllBatsman) {
            distinctPlayerList.size
        } else {
            ItemLoadMore.MIN_ITEM_DISPLAY
        }
        distinctPlayerList.take(size).forEachIndexed { index, player ->
            val color = if (player.isHomeTeam()) {
                awayParsedColor
            } else {
                homeParsedColor
            }
            itemBatman {
                id(player.id)
                batsmanName(player)
                listener(callbackSelect.get())
                selectLoading(state.isBatsmanSelectLoading)
                expired(state.gameExpired)
                teamColor(color)
            }
        }
    }
}
