package `in`.okcredit.merchant.customer_ui.ui.due_customer.views

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import kotlinx.android.synthetic.main.item_due_customer.view.*
import javax.annotation.Nullable

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DueCustomerItemView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var customer: Customer

    interface CustomerSelectionListener {
        fun onCustomerSelection(customer: Customer)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_due_customer, this, true)
    }

    @ModelProp
    fun setCustomer(customer: Customer) {
        this.customer = customer

        customer_name.text = customer.description

        balance_credit.text =
            context.getString(R.string.rupee_symbol) + CurrencyUtil.formatV2(customer.balanceV2).toString()

        val defaultPic = TextDrawable.builder()
            .buildRound(customer.description.substring(0, 1), ColorGenerator.MATERIAL.getColor(customer.description))

        val profileImage = customer.profileImage

        if (profileImage != null) {
            GlideApp.with(context)
                .load(Uri.parse(profileImage))
                .circleCrop()
                .placeholder(defaultPic)
                .fallback(defaultPic)
                .into(customer_pic)
        } else {
            customer_pic.setImageDrawable(defaultPic)
        }
    }

    @ModelProp
    fun selectCustomers(isChecked: Boolean) {
        select_customer.isChecked = isChecked
    }

    @CallbackProp
    fun setClickListener(@Nullable listener: CustomerSelectionListener?) {
        select_customer.setOnClickListener { listener?.onCustomerSelection(customer) }
    }
}
