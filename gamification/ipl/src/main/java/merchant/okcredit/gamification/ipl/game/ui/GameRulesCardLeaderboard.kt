package merchant.okcredit.gamification.ipl.game.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.GameRulesLeaderboardBinding
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isVisible
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class GameRulesCardLeaderboard @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var listener: OnGameRulesListener? = null

    private val binding: GameRulesLeaderboardBinding =
        GameRulesLeaderboardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        collapse()
        binding.apply {
            cvGameRules.setOnClickListener {
                listener?.onGameRuleInteracted()
                if (groupRules.isVisible()) {
                    listener?.onGameRuleCollapsed()
                    collapse()
                } else {
                    listener?.onGameRuleExpanded()
                    expand()
                }
            }
            ivArrow.setOnClickListener {
                listener?.onGameRuleInteracted()
                if (groupRules.isVisible()) {
                    listener?.onGameRuleCollapsed()
                    collapse()
                } else {
                    listener?.onGameRuleExpanded()
                    expand()
                }
            }
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
    }
}
