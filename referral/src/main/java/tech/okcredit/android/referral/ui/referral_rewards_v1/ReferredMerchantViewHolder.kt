package tech.okcredit.android.referral.ui.referral_rewards_v1

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import kotlinx.android.synthetic.main.item_referral_rewards.view.*
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isVisible
import tech.okcredit.android.base.extensions.visible

class ReferredMerchantViewHolder : EpoxyHolder() {

    lateinit var itemView: View

    override fun bindView(itemView: View) {
        this.itemView = itemView
        with(itemView) {
            closeBox.setOnClickListener {
                collapsedViews.visible()
                hideNextRewardsViews()
            }
        }
    }

    fun hideNextRewardsViews() {
        with(itemView) {
            expandedViews.gone()
            notifyButton.gone()
            notifyDisabledButton.gone()
        }
    }

    fun showNextRewardsView(canNotify: Boolean, disabledNotifyText: String?) {
        with(itemView) {
            expandedViews.visible()
            if (canNotify) {
                notifyButton.visible()
                notifyDisabledButton.gone()
            } else {
                notifyButton.gone()
                notifyDisabledButton.text = disabledNotifyText
                notifyDisabledButton.visible()
            }
        }
    }

    fun startOnFocusAnimation() {
        itemView
            .takeIf { it.notifyButton.isVisible() }
            ?.run { AnimationUtils.shakeV1(notifyButton) }
            ?: AnimationUtils.shakeV1(itemView.rootView)
    }

    fun stopOnFocusAnimation() {
        // Short Animation plays fast, keep an eye on crashes, if any
    }
}
