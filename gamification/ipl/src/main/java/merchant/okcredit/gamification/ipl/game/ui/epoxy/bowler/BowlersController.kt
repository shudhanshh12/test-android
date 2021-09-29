package merchant.okcredit.gamification.ipl.game.ui.epoxy.bowler

import android.content.Context
import android.graphics.Color
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Player
import merchant.okcredit.gamification.ipl.game.ui.GameContract
import merchant.okcredit.gamification.ipl.game.ui.epoxy.ItemLoadMore
import merchant.okcredit.gamification.ipl.game.ui.epoxy.ItemLoadMore.Companion.MIN_ITEM_DISPLAY
import merchant.okcredit.gamification.ipl.game.ui.epoxy.itemTeamName
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.getColorCompat
import javax.inject.Inject

class BowlersController @Inject constructor(
    private val callbackSelect: Lazy<ItemBowlers.BowlersListener>,
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

        val players = state.onboardingDetails?.bowlers?.allPlayers ?: return
        val showLoadMore = (players.size) > ItemLoadMore.LOAD_MORE_SIZE

        itemTeamName {
            id("bowlers_home_team_name")
            teamName(state.onboardingDetails?.teams?.homeTeam?.shortName)
            teamColor(awayParsedColor)
        }

        itemTeamName {
            id("bowlers_away_team_name")
            teamName(state.onboardingDetails?.teams?.awayTeam?.shortName)
            teamColor(homeParsedColor)
        }

        val playerListHome = state.onboardingDetails?.bowlers?.allPlayers?.filter { it.isHomeTeam() } as ArrayList
        val playerListAway = state.onboardingDetails?.bowlers?.allPlayers?.filter { it.isAwayTeam() } as ArrayList

        val playerList = state.onboardingDetails?.bowlers?.allPlayers as MutableList

        if (distinctPlayerList.isNullOrEmpty()) {
            try {
                playerList.forEachIndexed { index1, player1 ->
                    if (index1 % 2 != 0) {
                        if (playerListAway.isNullOrEmpty().not()) {
                            playerList[index1] = playerListAway[0]
                            playerListAway.removeAt(0)
                        }
                    } else {
                        if (playerListHome.isNullOrEmpty().not()) {
                            playerList[index1] = playerListHome[0]
                            playerListHome.removeAt(0)
                        }
                    }
                }
                if (playerListHome.isNullOrEmpty().not()) {
                    playerList.addAll(playerListHome)
                }
                distinctPlayerList = playerList.distinctBy { it.id }.toMutableList()
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }

        if (state.showAllBowler.not()) {
            distinctPlayerList.take(MIN_ITEM_DISPLAY).forEachIndexed { index, player ->
                val color = if (player.isHomeTeam()) {
                    awayParsedColor
                } else {
                    homeParsedColor
                }
                itemBowlers {
                    id(player.id)
                    bowlersName(player)
                    listener(callbackSelect.get())
                    selectLoading(state.isBowlersSelectLoading)
                    expired(state.gameExpired)
                    teamColor(color)
                }
            }
        } else {
            distinctPlayerList.forEachIndexed { index, player ->
                val color = if (player.isHomeTeam()) {
                    awayParsedColor
                } else {
                    homeParsedColor
                }
                itemBowlers {
                    id(player.id)
                    bowlersName(player)
                    listener(callbackSelect.get())
                    selectLoading(state.isBowlersSelectLoading)
                    expired(state.gameExpired)
                    teamColor(color)
                }
            }
        }
    }
}
