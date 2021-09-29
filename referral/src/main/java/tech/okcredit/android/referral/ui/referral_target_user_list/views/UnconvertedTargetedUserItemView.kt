package tech.okcredit.android.referral.ui.referral_target_user_list.views

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.referral.contract.models.TargetedUser
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.base.utils.TextDrawableUtils
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.databinding.ItemReferralUnconvertedTargetedUserBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class UnconvertedTargetedUserItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ItemReferralUnconvertedTargetedUserBinding =
        ItemReferralUnconvertedTargetedUserBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: UnconvertedTargetedUserActionListener? = null
    private var targetedUser: TargetedUser? = null

    init {
        binding.apply {
            inviteBtn.setOnClickListener {
                listener?.onInviteButtonClicked(targetedUser!!)
            }
        }
    }

    @ModelProp
    fun setTargetedUserItem(targetUser: TargetedUser) {
        binding.apply {
            referreeName.text = targetUser.name
            earnPrice.text =
                context.getString(R.string.earn, formatV2(targetUser.amount!!.toLong()))
            loadImage(targetUser)
        }
        this.targetedUser = targetUser
    }

    private fun loadImage(targetUser: TargetedUser) {
        val defaultPic = TextDrawableUtils
            .getRoundTextDrawable(binding.referreeName.text?.firstOrNull()?.toUpperCase().toString())
        if (targetUser.imageUrl.isNullOrBlank()) {
            binding.targetedUserImage.setImageDrawable(defaultPic)
        } else {
            GlideApp.with(context)
                .load(targetUser.imageUrl)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .thumbnail(0.25f)
                .into(binding.targetedUserImage)
        }
    }

    interface UnconvertedTargetedUserActionListener {
        fun onInviteButtonClicked(targetUser: TargetedUser)
    }

    @CallbackProp
    fun setListener(listener: UnconvertedTargetedUserActionListener?) {
        this.listener = listener
    }
}
