package tech.okcredit.android.referral.ui.referral_rewards_v1.views

import `in`.okcredit.fileupload.usecase.IImageLoader
import androidx.annotation.LayoutRes
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import kotlinx.android.synthetic.main.item_referral_rewards.view.*
import kotlinx.android.synthetic.main.layout_next_reward.view.*
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.data.ReferredMerchant
import tech.okcredit.android.referral.ui.referral_rewards_v1.ReferredMerchantViewHolder

@EpoxyModelClass
open class ReferralRewardsItemModel constructor(
    private var referredMerchant: ReferredMerchant,
    private val imageLoader: IImageLoader,
    private val onNotifyClicked: ((String) -> Unit)?,
    private val onEarnMoreClicked: (() -> Unit)?,
    private var isExpandedInitially: Boolean,
) : EpoxyModelWithHolder<ReferredMerchantViewHolder>() {

    @LayoutRes
    override fun getDefaultLayout() = R.layout.item_referral_rewards

    override fun createNewHolder() = ReferredMerchantViewHolder()

    override fun bind(holder: ReferredMerchantViewHolder) {
        super.bind(holder)

        referredMerchant.let {
            updateMerchantDetails(holder, it)
            loadImage(holder, it)
            updateNextReward(holder, it)
            initListeners(holder, it)

            if (isExpandedInitially) {
                expandRewardBox(holder, it)
                animateRewardBox(holder)
                isExpandedInitially = false
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ReferredMerchantViewHolder) {
        stopRewardBoxAnimation(holder)
        super.onViewDetachedFromWindow(holder)
    }

    private fun updateMerchantDetails(holder: ReferredMerchantViewHolder, referredMerchant: ReferredMerchant) {
        with(holder.itemView) {
            if (referredMerchant.name.isNullOrBlank()) {
                nameTextView.text = referredMerchant.phoneNumber
                mobileTextView.gone()
            } else {
                nameTextView.text = referredMerchant.name
                mobileTextView.text = referredMerchant.phoneNumber
                mobileTextView.visible()
            }
            val pendingAmount = referredMerchant.pendingAmount?.toLongOrNull()
            if (pendingAmount != null && pendingAmount != 0L) {
                pendingRewardAmountTextView.text =
                    context.getString(R.string.rupee_placeholder, formatV2(pendingAmount))
                pendingRewardViews.visible()
            } else {
                pendingRewardViews.gone()
            }
        }
    }

    private fun loadImage(holder: ReferredMerchantViewHolder, referredMerchant: ReferredMerchant) {
        with(holder.itemView) {
            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    nameTextView.text?.firstOrNull()?.toUpperCase().toString(),
                    ColorGenerator.MATERIAL.getColor(nameTextView.text)
                )
            if (referredMerchant.imageUrl.isNullOrBlank()) {
                displayPictureImageView.setImageDrawable(defaultPic)
            } else {
                imageLoader.context(holder.itemView.context)
                    .load(referredMerchant.imageUrl)
                    .placeHolder(defaultPic)
                    .scaleType(IImageLoader.CIRCLE_CROP)
                    .into(displayPictureImageView)
                    .buildNormalWithPlaceholder()
            }
        }
    }

    private fun updateNextReward(holder: ReferredMerchantViewHolder, referredMerchant: ReferredMerchant) {
        with(holder.itemView) {
            if (!referredMerchant.rewards?.get(0)?.events.isNullOrEmpty()) {
                val reward = referredMerchant.rewards?.get(0)!!
                nextRewardTitleTextView.text = reward.referrerTitle
                nextRewardDescriptionTextView.text = reward.referrerDescription
                reward.referrerPrize?.toLongOrNull()?.let {
                    nextRewardAmountTextView.text =
                        context.getString(R.string.rupee_placeholder, formatV2(it))
                }
                collapsedViews.visible()
            } else {
                collapsedViews.gone()
            }
            holder.hideNextRewardsViews()
        }
    }

    private fun initListeners(holder: ReferredMerchantViewHolder, referredMerchant: ReferredMerchant) {
        with(holder.itemView) {
            moreRewardsBox.setOnClickListener {
                expandRewardBox(holder, referredMerchant)
                onEarnMoreClicked?.invoke()
            }
            notifyButton.setOnClickListener { onNotifyClicked?.invoke(referredMerchant.phoneNumber!!) }
        }
    }

    private fun expandRewardBox(holder: ReferredMerchantViewHolder, referredMerchant: ReferredMerchant) {
        with(holder.itemView) {
            collapsedViews.gone()
            holder.showNextRewardsView(
                referredMerchant.canNotify,
                context.getString(R.string.notify_later, referredMerchant.enableTime)
            )
        }
    }

    private fun animateRewardBox(holder: ReferredMerchantViewHolder) {
        holder.startOnFocusAnimation()
    }

    private fun stopRewardBoxAnimation(holder: ReferredMerchantViewHolder) {
        holder.stopOnFocusAnimation()
    }
}
