package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ItemStaffLinkSelectCustomerBinding
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.addRipple
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.utils.TextDrawableUtils
import kotlin.math.roundToInt

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class StaffLinkAddCustomerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding = ItemStaffLinkSelectCustomerBinding.inflate(LayoutInflater.from(context), this)

    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    private var customerSelectionListener: SelectCustomerListener? = null

    private var customerId: String = ""

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancelChildren()
    }

    init {
        binding.root.addRipple()
        binding.root.setPadding(
            context.dpToPixel(16f).roundToInt(),
            context.dpToPixel(16f).roundToInt(),
            context.dpToPixel(16f).roundToInt(),
            0
        )

        binding.root.setOnClickListener {
            customerSelectionListener?.onCustomerSelected(customerId)
        }
    }

    @CallbackProp
    fun setListener(listener: SelectCustomerListener?) {
        this.customerSelectionListener = listener
    }

    @ModelProp
    fun setCustomerItem(customerItem: CustomerItem) {
        customerId = customerItem.id
        binding.textName.text = customerItem.name
        binding.textSubtitle.text = customerItem.subTitle
        setCustomerProfilePic(customerItem.name, customerItem.profilePic)
        if (customerItem.selected) {
            binding.imageSelected.imageTintList = null
        } else {
            binding.imageSelected.imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.grey400))
        }
    }

    private fun setCustomerProfilePic(name: String, profileImage: String?) {
        viewScope.launch {
            val defaultPic = TextDrawableUtils.getRoundTextDrawable(name)

            withContext(Dispatchers.Main) {
                GlideApp.with(context)
                    .load(profileImage)
                    .placeholder(defaultPic)
                    .circleCrop()
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .thumbnail(0.25f)
                    .into(binding.imageProfile)
            }
        }
    }

    interface SelectCustomerListener {
        fun onCustomerSelected(customerId: String)
    }
}
