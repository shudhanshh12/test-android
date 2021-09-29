package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ItemStaffLinkEditCustomerBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class StaffLinkEditDetailsCustomerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = ItemStaffLinkEditCustomerBinding.inflate(LayoutInflater.from(context), this, true)

    private var viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + viewJob)

    private var customerSelectionListener: SelectCustomerListener? = null

    private var customerId: String = ""

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancelChildren()
    }

    init {
        binding.root.setOnClickListener {
            customerSelectionListener?.onCustomerClicked(customerId)
        }
        binding.imageDelete.setOnClickListener {
            customerSelectionListener?.onCustomerDeleteClicked(customerId)
        }
        binding.textAddressEdit.setOnClickListener {
            customerSelectionListener?.onCustomerUpdateAddressClicked(customerId)
        }
        binding.textMobileEdit.setOnClickListener {
            customerSelectionListener?.onCustomerUpdateMobileClicked(customerId)
        }
    }

    @CallbackProp
    fun setListener(listener: SelectCustomerListener?) {
        this.customerSelectionListener = listener
    }

    @ModelProp
    fun setCustomerItem(customerItem: StaffLinkEditDetailsCustomerItem) {
        customerId = customerItem.id
        binding.textName.text = customerItem.name

        setPaymentReceived(customerItem.showPaymentReceived, customerItem.lasPayment)
        setCustomerBalance(customerItem.balance)
        setCustomerProfilePic(customerItem.name, customerItem.profilePic)
        setCustomerMobile(customerItem.mobile)
        setCustomerAddress(customerItem.address)
    }

    private fun setPaymentReceived(showPaymentReceived: Boolean, lasPayment: DateTime?) {
        if (showPaymentReceived && lasPayment != null) {
            val formattedTime = DateTimeUtils.formatAccountStatement(context, lasPayment)
            binding.textPaymentReceived.text = context.getString(R.string.t_003_staff_collection_success, formattedTime)
            binding.textPaymentReceived.visible()
            binding.view.visible()
        } else {
            binding.textPaymentReceived.gone()
            binding.view.invisible()
        }
    }

    private fun setCustomerMobile(mobile: String?) {
        if (mobile.isNullOrEmpty()) {
            binding.textMobileEdit.visible()
            binding.textMobileEdit.text = getString(R.string.custpr_add_mobile)

            binding.textMobile.gone()
        } else {
            binding.textMobileEdit.gone()

            binding.textMobile.visible()
            binding.textMobile.text = mobile
        }
    }

    private fun setCustomerAddress(address: String?) {
        if (address.isNullOrEmpty()) {
            binding.textAddressEdit.visible()
            binding.textAddressEdit.text = getString(R.string.custpr_add_address)

            binding.textAddress.gone()
        } else {
            binding.textAddressEdit.gone()

            binding.textAddress.visible()
            binding.textAddress.text = address
        }
    }

    private fun setCustomerBalance(balance: Long) {
        binding.textSubtitle.text = when {
            balance < 0 -> {
                buildSpannedString {
                    color(context.getColorCompat(R.color.tx_credit)) {
                        append(String.format("₹%s", CurrencyUtil.formatV2(balance)))
                    }
                    append(" ")
                    append(getString(R.string.due))
                }
            }
            else -> {
                buildSpannedString {
                    color(context.getColorCompat(R.color.tx_payment)) {
                        append(String.format("₹%s", CurrencyUtil.formatV2(balance)))
                    }
                    append(" ")
                    append(getString(R.string.advance))
                }
            }
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
        fun onCustomerClicked(customerId: String)
        fun onCustomerDeleteClicked(customerId: String)
        fun onCustomerUpdateMobileClicked(customerId: String)
        fun onCustomerUpdateAddressClicked(customerId: String)
    }
}
