package `in`.okcredit.collection_ui.ui.insights.views

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection_ui.R
import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.Nullable
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.item_defaulters.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DefaultersItemView @JvmOverloads constructor(
    ctx: Context,
    val attr: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attr, defStyleAttr) {

    private lateinit var customer: Customer

    interface DefaulterCustomersSelectionListener {
        fun onCustomerSelection(customer: Customer)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_defaulters, this, true)
    }

    @ModelProp
    fun setImage(customer: Customer) {

        this.customer = customer

        val defaultPic = TextDrawable.builder()
            .buildRound(customer.description.substring(0, 1), ColorGenerator.MATERIAL.getColor(customer.description))

        val profileImage = customer.profileImage ?: null

        if (profileImage != null) {

            GlideApp.with(context)
                .load(profileImage)
                .circleCrop()
                .placeholder(defaultPic)
                .fallback(defaultPic)
                .into(defaulter_icon)
        } else {
            defaulter_icon?.setImageDrawable(defaultPic)
        }
    }

    @ModelProp
    fun setName(name: String) {
        defaulter_name?.text = name
    }

    @ModelProp
    fun setAmount(amount: Long) {
        defaulter_amount?.text = String.format(
            "${context.getString(R.string.rupee_symbol)} %s",
            CurrencyUtil.formatV2(
                amount
                    ?: 0L
            )
        )
    }

    @CallbackProp
    fun setClickListener(@Nullable listener: DefaulterCustomersSelectionListener?) {
        defaulter_customer.clicks()
            .doOnNext {
                listener?.onCustomerSelection(customer)
            }.subscribe()
    }
}
