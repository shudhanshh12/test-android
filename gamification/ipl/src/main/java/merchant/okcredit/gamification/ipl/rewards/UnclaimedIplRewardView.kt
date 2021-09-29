package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.rewards.contract.RewardModel
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.UnclaimedIplRewardBinding
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.ifLet
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class UnclaimedIplRewardView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = UnclaimedIplRewardBinding.inflate(LayoutInflater.from(context), this)
    private var rewardModel: RewardModel? = null

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

    init {
        binding.root.setOnClickListener {
            rewardModel?.let { it1 -> ClaimRewardActivity.start(context as Activity, it1, source?.invoke() ?: "") }
            ifLet(eventTracker?.invoke(), source?.invoke()) { tracker, source ->
                tracker.rewardClicked(source, rewardModel?.reward_type ?: "")
            }
        }
    }

    @ModelProp
    fun setReward(rewards: RewardModel) {
        binding.ivImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_gift_reward))
        binding.apply {
            when {
                rewards.isOnHold() -> {
                    tvTitle.gone()
                    rewardsOnHold.visible()
                    if (rewards.isEditBankDetails()) {
                        rewardsOnHold.text = context.getString(R.string.edit_bank_details)
                    }
                    TextViewCompat.setCompoundDrawableTintList(
                        rewardsOnHold,
                        ColorStateList.valueOf(context.getColorCompat(R.color.red_primary))
                    )
                    rewardsOnHold.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_error_16px,
                        0,
                        0,
                        0
                    )
                }
                rewards.isFailed() -> {
                    tvTitle.gone()
                    rewardsOnHold.visible()
                    rewardsOnHold.text = context.getString(R.string.please_try_again)
                    TextViewCompat.setCompoundDrawableTintList(
                        rewardsOnHold,
                        ColorStateList.valueOf(context.getColorCompat(R.color.red_primary))
                    )
                    rewardsOnHold.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_error_16px,
                        0,
                        0,
                        0
                    )
                }
                rewards.isProcessing() -> {
                    tvTitle.gone()
                    rewardsOnHold.visible()
                    rewardsOnHold.text = context.getString(R.string.processing_state)
                }
                else -> {
                    tvTitle.visible()
                    rewardsOnHold.gone()
                }
            }
        }
        this.rewardModel = rewards
    }
}
