package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.rewards.contract.RewardModel
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.ClaimedBatRewardSmallBinding
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import tech.okcredit.android.base.extensions.ifLet

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ClaimedBatRewardView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ClaimedBatRewardSmallBinding.inflate(LayoutInflater.from(context), this)

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
    fun setReward(reward: RewardModel) {
        binding.root.setOnClickListener {
            ClaimRewardActivity.start(context as Activity, reward, source?.invoke() ?: "")
            ifLet(eventTracker?.invoke(), source?.invoke()) { tracker, source ->
                tracker.rewardClicked(source, reward.reward_type ?: "")
            }
        }
    }
}
