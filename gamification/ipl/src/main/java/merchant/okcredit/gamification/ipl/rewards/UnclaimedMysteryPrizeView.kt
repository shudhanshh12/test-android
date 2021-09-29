package merchant.okcredit.gamification.ipl.rewards

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.UnclaimedMysteryPrizeBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.ifLet

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class UnclaimedMysteryPrizeView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = UnclaimedMysteryPrizeBinding.inflate(LayoutInflater.from(context), this)

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
        if (prize.isWelcomeReward()) {
            binding.apply {
                reward.text = resources?.getString(R.string.mystery_welcome_reward_title)
                reward.setTextColor(context.getColorCompat(R.color.white))
                reward.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_welcome_gift, 0, 0)
                mbReward.setCardBackgroundColor(context.getColorCompat(R.color.indigo_1))
            }
        } else {
            binding.apply {
                reward.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mystery_reward, 0, 0)
                reward.setTextColor(context.getColorCompat(R.color.grey900))
                mbReward.setCardBackgroundColor(context.getColorCompat(R.color.indigo_lite))
            }
        }

        binding.root.setOnClickListener {
            ClaimRewardActivity.start(context as Activity, prize, source?.invoke() ?: "")
            ifLet(eventTracker?.invoke(), source?.invoke()) { tracker, source ->
                tracker.mysteryPrizeClicked(source)
            }
        }
    }
}
