package merchant.okcredit.gamification.ipl.rewards.mysteryprize

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ClaimedMysteryPrizeSmallBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardActivity
import tech.okcredit.android.base.extensions.ifLet

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ClaimedMysteryPrizeView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ClaimedMysteryPrizeSmallBinding.inflate(LayoutInflater.from(context), this)

    private var eventTracker: (() -> IplEventTracker?)? = null
    private var source: (() -> String?)? = null

    @CallbackProp
    fun setEventTracker(eventTracker: (() -> IplEventTracker?)?) {
        this.eventTracker = eventTracker
    }

    @CallbackProp
    fun setSource(source: (() -> String?)?) {
        this.source = source
    }

    @ModelProp
    fun setPrize(prize: MysteryPrizeModel) {
        binding.points.text = context.getString(R.string.points_placeholder, prize.amount.toString())
        binding.root.setOnClickListener {
            ClaimRewardActivity.start(context as Activity, prize, source?.invoke() ?: "")
            ifLet(eventTracker?.invoke(), source?.invoke()) { tracker, source ->
                tracker.mysteryPrizeClicked(source)
            }
        }
    }
}
