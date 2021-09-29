package `in`.okcredit.merchant.rewards.ui.rewards_screen.views

import `in`.okcredit.analytics.Event
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.databinding.UnclaimedRewardViewBinding
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardActivity
import `in`.okcredit.rewards.contract.RewardModel
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class UnclaimedActivationReward @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var rewardModel: RewardModel? = null

    private val binding: UnclaimedRewardViewBinding =
        UnclaimedRewardViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.root.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .doOnNext {
                rewardModel?.let { model ->
                    ClaimRewardActivity.start(context as Activity, model, Event.REWARD_SCREEN)
                }
            }
            .subscribe()
    }

    @ModelProp
    fun setReward(rewards: RewardModel) {
        binding.ivImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_reward_type_feature))
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
