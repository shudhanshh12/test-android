package `in`.okcredit.merchant.ui.select_business.view

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.merchant.databinding.ViewSelectBusinessItemBinding
import `in`.okcredit.merchant.ui.select_business.SelectBusinessContract
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.android.base.utils.TextDrawableUtils
import kotlin.math.abs

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SelectBusinessItemView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: ViewSelectBusinessItemBinding =
        ViewSelectBusinessItemBinding.inflate(LayoutInflater.from(context), this)

    private var listener: Listener? = null

    interface Listener {
        fun onSelect(businessId: String, businessName: String)
    }

    init {
        setBackgroundResource(R.drawable.background_white_corner_radius_8dp_with_1dp_stroke)
        elevation = 8f
        val padding = DimensionUtil.dp2px(context, 20f).toInt()
        setPadding(0, padding, 0, padding)
    }

    @ModelProp
    fun setBusinessData(businessData: SelectBusinessContract.BusinessData) {
        setProfileImage(businessData.business)
        binding.businessName.text = businessData.business.name
        binding.root.setOnClickListener {
            listener?.onSelect(businessData.business.id, businessData.business.name)
        }

        if (businessData.balanceAmount != null) {
            binding.tvNetBalanceText.text = getNetBalanceText(businessData.balanceAmount)
            binding.tvNetBalanceText.visible()
            binding.tvNetBalance.visible()
        } else {
            binding.tvNetBalanceText.gone()
            binding.tvNetBalance.gone()
        }
    }

    private fun getNetBalanceText(balanceAmount: Long): SpannableStringBuilder {
        val netBalanceText = context.getString(R.string.t_001_multi_acc_net_bal)
        val getBalanceType = getStringForAmount(balanceAmount)
        val balanceValue = abs(balanceAmount / 100.0).toString()
        val balance = SpannableStringBuilder(netBalanceText)
        balance.append(" ")
        balance.append(balanceValue)
        if (balanceAmount >= 0) {
            balance.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.green_primary
                    )
                ),
                13, balance.length, 0
            )
        } else {
            balance.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.red_primary
                    )
                ),
                13, balance.length, 0
            )
        }
        balance.append(" ")
        balance.append(getBalanceType)
        return balance
    }

    private fun getStringForAmount(balance: Long): String {
        val id = if (balance >= 0) R.string.advance
        else R.string.due
        return context.getString(id)
    }

    private fun setProfileImage(business: Business) {
        val defaultPic = TextDrawableUtils.getRoundTextDrawable(business.name)
        Glide.with(this)
            .load(business.profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .into(binding.profileImage)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
