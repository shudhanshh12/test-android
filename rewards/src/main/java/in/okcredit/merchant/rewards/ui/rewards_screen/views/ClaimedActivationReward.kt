package `in`.okcredit.merchant.rewards.ui.rewards_screen.views

import `in`.okcredit.analytics.Event
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.databinding.ClaimedRewardsViewBinding
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardActivity
import `in`.okcredit.rewards.contract.RewardModel
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import tech.okcredit.android.base.TempCurrencyUtil
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ClaimedActivationReward @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var rewardModel: RewardModel? = null

    private val binding: ClaimedRewardsViewBinding =
        ClaimedRewardsViewBinding.inflate(LayoutInflater.from(context), this, true)

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
    fun setReward(reward: RewardModel) {
        binding.apply {
            if (reward.isFeatureRewards()) {
                tvTitle.text = context.getString(R.string.you_won_feature, reward.featureTitle)
                GlideApp
                    .with(context)
                    .load(reward.icon)
                    .placeholder(R.drawable.ic_activation_claimed_rewards)
                    .error(R.drawable.ic_activation_claimed_rewards)
                    .fallback(R.drawable.ic_activation_claimed_rewards)
                    .centerCrop()
                    .into(ivImage)
            } else {
                tvTitle.text = context.getString(R.string.you_won, TempCurrencyUtil.formatV2(reward.amount))
            }

            cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            ivImage.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_activation_claimed_rewards
                )
            )
        }

        this.rewardModel = reward
    }
}
