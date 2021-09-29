package merchant.okcredit.gamification.ipl.leaderboard.epoxy.controller

import android.content.Context
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.game.data.server.model.response.LeaderBoardResponse
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardLeaderboard
import merchant.okcredit.gamification.ipl.game.ui.gameRulesCardLeaderboard
import merchant.okcredit.gamification.ipl.leaderboard.epoxy.view.merchantSelfScore
import merchant.okcredit.gamification.ipl.leaderboard.epoxy.view.otherMerchantScore
import merchant.okcredit.gamification.ipl.leaderboard.epoxy.view.prizeHeader
import tech.okcredit.android.base.extensions.getColorCompat
import javax.inject.Inject

class LeaderBoardController @Inject constructor(
    private val context: Lazy<Context>,
    private val callback: Lazy<GameRulesCardLeaderboard.OnGameRulesListener>,
) : AsyncEpoxyController() {

    private var data: LeaderBoardResponse? = null
    private var collapsed: Boolean = false
    fun setRulesCollapsed(collapsed: Boolean) {
        this.collapsed = collapsed
        requestModelBuild()
    }

    fun setData(data: LeaderBoardResponse?) {
        this.data = data
        requestModelBuild()
    }

    override fun buildModels() {

        gameRulesCardLeaderboard {
            id("leader_board")
            listener(callback.get())
            rulesCollapse(collapsed)
        }

        merchantSelfScore {
            id("merchantSelfScore$modelCountBuiltSoFar")
            selfDetails(data?.merchantScore)
        }

        val goldPrize = data?.topItems?.filter { it.isGoldPrize() }
        val money10kPrize = data?.topItems?.filter { it.is10KMoneyPrize() }
        val money5kPrize = data?.topItems?.filter { it.is5KMoneyPrize() }
        val money2_5kPrize = data?.topItems?.filter { it.is2_5KMoneyPrize() }
        val money1_5kPrize = data?.topItems?.filter { it.is1_5KMoneyPrize() }
        val money1kPrize = data?.topItems?.filter { it.is1KMoneyPrize() }
        val jerseyPrize = data?.topItems?.filter { it.isJerseyPrize() }
        val miniBatPrize = data?.topItems?.filter { it.isMiniBatPrize() }

        if (goldPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.orange_lite_1))
                prizeHeaderIcon(R.drawable.ic_gold_coin)
                prizeHeaderTes(context.get().getString(R.string.title_gold_prize))
            }
            goldPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (money10kPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.green_lite_1))
                prizeHeaderIcon(R.drawable.ic_cash_prize)
                prizeHeaderTes(context.get().getString(R.string.title_cash_prize_10k))
            }
            money10kPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (money5kPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.green_lite_1))
                prizeHeaderIcon(R.drawable.ic_cash_prize)
                prizeHeaderTes(context.get().getString(R.string.title_cash_prize_5k))
            }
            money5kPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (money2_5kPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.green_lite_1))
                prizeHeaderIcon(R.drawable.ic_cash_prize)
                prizeHeaderTes(context.get().getString(R.string.title_cash_prize_2_5k))
            }
            money2_5kPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (money1_5kPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.green_lite_1))
                prizeHeaderIcon(R.drawable.ic_cash_prize)
                prizeHeaderTes(context.get().getString(R.string.title_cash_prize_1_5k))
            }
            money1_5kPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (money1kPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.green_lite_1))
                prizeHeaderIcon(R.drawable.ic_cash_prize)
                prizeHeaderTes(context.get().getString(R.string.title_cash_prize_1k))
            }
            money1kPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (jerseyPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.indigo_lite))
                prizeHeaderIcon(R.drawable.ic_jersey)
                prizeHeaderTes(context.get().getString(R.string.title_prize_t_shirts))
            }
            jerseyPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }

        if (miniBatPrize.isNullOrEmpty().not()) {
            prizeHeader {
                id("prizeHeader$modelCountBuiltSoFar")
                prizeHeaderColor(context.get().getColorCompat(R.color.red_lite))
                prizeHeaderIcon(R.drawable.ic_mini_bat)
                prizeHeaderTes(context.get().getString(R.string.title_prize_mini_bats))
            }
            miniBatPrize?.forEach {
                otherMerchantScore {
                    id("otherMerchantScore$modelCountBuiltSoFar")
                    otherMerchantDetails(it)
                    scoreVisibility(data?.merchantScore?.hideScore() == true)
                }
            }
        }
    }
}
