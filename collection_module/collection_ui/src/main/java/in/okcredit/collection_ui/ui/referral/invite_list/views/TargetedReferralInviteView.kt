package `in`.okcredit.collection_ui.ui.referral.invite_list.views

import `in`.okcredit.collection.contract.ReferralStatus
import `in`.okcredit.collection.contract.TargetedCustomerReferralInfo
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ReferralInviteListViewBinding
import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.base.extensions.colorStateList
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TargetedReferralInviteView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private var listener: Listener? = null
    private lateinit var targetedCustomerReferralInfo: TargetedCustomerReferralInfo

    interface Listener {
        fun inviteOnWhatsApp(targetedCustomerReferralInfo: TargetedCustomerReferralInfo)
    }

    private val binding: ReferralInviteListViewBinding =
        ReferralInviteListViewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setReferralInfo(referralInfo: TargetedCustomerReferralInfo) {
        targetedCustomerReferralInfo = referralInfo
        binding.apply {
            refereeName.text = targetedCustomerReferralInfo.description
            setSubText(referralInfo.status)
            loadImage(targetedCustomerReferralInfo)
        }
    }

    private fun setSubText(action: Int) {
        binding.earnPrice.text = when (action) {
            ReferralStatus.LINK_CREATED.value -> {
                setInviteBtnUi()
                binding.earnPrice.setTextColor(context.getColorCompat(R.color.indigo_1))
                context.getString(
                    R.string.invite_earn_share_list_subtext,
                    formatV2(targetedCustomerReferralInfo.amount)
                )
            }
            ReferralStatus.LINK_SHARED.value -> {
                setRemindBtnUi()
                binding.earnPrice.setTextColor(context.getColorCompat(R.color.grey700))
                context.getString(R.string.invite_earn_share_list_subtext_1)
            }
            ReferralStatus.DESTINATION_ADDED.value -> {
                setRemindBtnUi()
                binding.earnPrice.setTextColor(context.getColorCompat(R.color.grey700))
                context.getString(R.string.invite_earn_share_list_subtext_2)
            }
            ReferralStatus.REWARD_SUCCESS.value -> {
                binding.inviteBtn.gone()
                binding.earnPrice.setTextColor(context.getColorCompat(R.color.green_primary))
                binding.earnPrice.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_success_16dp,
                    0
                )
                context.getString(R.string.invite_earn_share_list_subtext_4)
            }
            ReferralStatus.REWARD_CREATED.value,
            ReferralStatus.PAYMENT_RECEIVED.value,
            ReferralStatus.REWARD_PROCESSING.value,
            ReferralStatus.REWARD_FAILED.value,
            -> {
                binding.inviteBtn.gone()
                binding.earnPrice.setTextColor(context.getColorCompat(R.color.green_primary))
                context.getString(R.string.invite_earn_share_list_subtext_3)
            }
            else -> {
                setInviteBtnUi()
                context.getString(
                    R.string.invite_earn_share_list_subtext,
                    formatV2(targetedCustomerReferralInfo.amount)
                )
            }
        }
    }

    private fun setRemindBtnUi() {

        binding.apply {
            inviteBtn.visible()
            inviteBtn.text = context.getString(R.string.remind)
            inviteBtn.setTextColor(context.colorStateList(R.color.grey900))
            inviteBtn.strokeColor = context.colorStateList(R.color.grey400)
            inviteBtn.iconTint = context.colorStateList(R.color.grey900)
            earnPrice.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
        }
    }

    private fun setInviteBtnUi() {
        binding.apply {
            inviteBtn.visible()
            inviteBtn.text = context.getString(R.string.invite)
            inviteBtn.setTextColor(context.colorStateList(R.color.green_primary))
            inviteBtn.strokeColor = context.colorStateList(R.color.green_primary)
            inviteBtn.iconTint = context.colorStateList(R.color.green_primary)
            earnPrice.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
        }
    }

    private fun loadImage(referralInfo: TargetedCustomerReferralInfo) {
        val defaultPic = TextDrawableUtils
            .getRoundTextDrawable(binding.refereeName.text?.firstOrNull()?.uppercaseChar().toString())
        if (referralInfo.profileImage.isNullOrBlank()) {
            binding.targetedUserImage.setImageDrawable(defaultPic)
        } else {
            GlideApp.with(context)
                .load(referralInfo.profileImage)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .thumbnail(0.25f)
                .into(binding.targetedUserImage)
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    init {
        binding.inviteBtn.setOnClickListener {
            listener?.inviteOnWhatsApp(targetedCustomerReferralInfo)
        }
    }
}
