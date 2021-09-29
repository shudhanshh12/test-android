package merchant.okcredit.gamification.ipl.game.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.GameRulesWeeklyBinding
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isVisible
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class GameRulesCardWeekly @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var listener: OnGameRulesListener? = null

    private val binding: GameRulesWeeklyBinding =
        GameRulesWeeklyBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        collapse()
        binding.apply {
            cvGameRules.setOnClickListener {
                if (groupRules.isVisible()) {
                    listener?.onGameRuleCollapsed()
                    collapse()
                } else {
                    listener?.onGameRuleExpanded()
                    expand()
                }
            }
            ivArrow.setOnClickListener {
                if (groupRules.isVisible()) {
                    listener?.onGameRuleCollapsed()
                    collapse()
                } else {
                    listener?.onGameRuleExpanded()
                    expand()
                }
            }

            tvHowToPlayGame.setOnClickListener {
                listener?.onHowToPlayOkPl()
            }
        }
    }

    @ModelProp
    fun setRulesData(rules: SundayGameModel.GameRules) {
        binding.apply {
            tvBody1.text = resources?.getString(R.string.rules_answer_any, rules.boosterCount.toString())
            tvBody2.text = resources?.getString(R.string.rules_weekly_score, rules.totalRuns.toString())
            tvSub3.text = resources?.getString(
                R.string.rules_weekly_available,
                DateTimeUtils.getFormat4(DateTime(rules.date))
            )
        }
    }

    @ModelProp
    fun setRulesCollapse(collapse: Boolean) {
        if (collapse) {
            collapse()
        }
    }

    @CallbackProp
    fun setListener(listener: OnGameRulesListener?) {
        this.listener = listener
    }

    private fun expand() {
        binding.apply {
            groupRules.visible()
            ivArrow.rotation = 180F
        }
    }

    private fun collapse() {
        binding.apply {
            groupRules.gone()
            ivArrow.rotation = 360F
        }
    }

    interface OnGameRulesListener {
        fun onGameRuleInteracted()
        fun onGameRuleExpanded()
        fun onGameRuleCollapsed()
        fun onHowToPlayOkPl()
    }
}
