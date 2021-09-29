package merchant.okcredit.gamification.ipl.game.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.IplGameRulesCardBinding
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isVisible
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.web.ui.WebViewActivity

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class GameRulesCard @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    companion object {
        const val TERM_AND_CONDITIONS_URL = "https://okipl.okcredit.in/terms"
    }

    private var listener: OnGameRulesListener? = null

    private val binding: IplGameRulesCardBinding =
        IplGameRulesCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        collapse()
        binding.apply {
            cvGameRules.setOnClickListener {
                listener?.onGameRuleInteracted()
                if (groupRules.isVisible()) {
                    collapse()
                } else {
                    listener?.onGameRuleExpanded()
                    expand()
                }
            }
            ivArrow.setOnClickListener {
                listener?.onGameRuleInteracted()
                if (groupRules.isVisible()) {
                    collapse()
                } else {
                    listener?.onGameRuleExpanded()
                    expand()
                }
            }
            tvTermsAndConditions.setOnClickListener {
                WebViewActivity.start(context, TERM_AND_CONDITIONS_URL)
            }
        }
    }

    @ModelProp
    fun setGameRules(gameRules: String) {
        binding.tvRules.text = gameRules
    }

    @ModelProp
    fun setTitle(title: String) {
        binding.tvTitleGameRules.text = title
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
    }
}
