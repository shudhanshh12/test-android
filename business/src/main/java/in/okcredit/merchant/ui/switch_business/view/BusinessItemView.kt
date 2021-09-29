package `in`.okcredit.merchant.ui.switch_business.view

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessEvents.Value.POST_PROFILE_SECTION
import `in`.okcredit.merchant.merchant.databinding.SwitchBusinessItemViewBinding
import `in`.okcredit.merchant.ui.switch_business.SwitchBusinessContract
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BusinessItemView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: SwitchBusinessItemViewBinding =
        SwitchBusinessItemViewBinding.inflate(LayoutInflater.from(context), this)

    private var listener: Listener? = null
    private var source: String? = null

    interface Listener {
        fun onSelect(businessId: String, businessName: String)
        fun onEdit(businessId: String)
        fun dismiss()
    }

    @ModelProp
    fun setBusiness(businessModel: SwitchBusinessContract.BusinessModel) {
        setProfileImage(businessModel.business)
        setBusinessNameAndMobile(businessModel.business)
        binding.editProfile.isVisible = businessModel.isActive
        binding.mobile.isVisible = businessModel.isActive
        binding.viewDivider.isVisible = businessModel.isActive
        binding.editProfile.setOnClickListener {
            if (source != POST_PROFILE_SECTION) {
                listener?.onEdit(businessModel.business.id)
            } else {
                listener?.dismiss()
            }
        }
        binding.root.setOnClickListener {
            if (businessModel.isActive.not()) {
                listener?.onSelect(businessModel.business.id, businessModel.business.name)
                binding.loader.visible()
            } else {
                listener?.dismiss()
            }
        }
    }

    @ModelProp
    fun setSource(source: String?) {
        this.source = source
    }

    private fun setProfileImage(business: Business) {
        val defaultPic = TextDrawableUtils.getRoundTextDrawable(business.name)
        Glide.with(this)
            .load(business.profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .into(binding.profileImage)
    }

    private fun setBusinessNameAndMobile(business: Business) {
        binding.businessName.text = business.name
        binding.mobile.text = business.mobile
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
