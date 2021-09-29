package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views

import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.ItemCustomerListBinding
import `in`.okcredit.user_migration.presentation.data.UserMigrationApiMessages
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemViewCustomerList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ItemCustomerListBinding =
        ItemCustomerListBinding.inflate(LayoutInflater.from(context), this, true)

    private var customerAndTransaction: CustomerUiTemplate? = null
    private var listener: ItemViewCustomerListener? = null

    init {
        initClickListener()
    }

    private fun initClickListener() {
        binding.apply {
            downArrow.setOnClickListener {
                customerAndTransaction?.also { customer ->
                    listener?.showEditDetailsDialog(customer)
                }
            }

            binding.cbCustomer.setOnClickListener {
                if (customerAndTransaction?.error == false) {
                    customerAndTransaction?.also { customer ->
                        listener?.removeCustomer(
                            customer.copy(
                                isCheckedBoxChecked = (it as CheckBox).isChecked,
                                error = false
                            )
                        )
                    }
                } else {
                    cbCustomer.isChecked = false
                    listener?.showSolveErrorMessage()
                }
            }
        }
    }

    @ModelProp
    fun setCustomerAndTransaction(customerAndTransaction: CustomerUiTemplate) {
        binding.apply {
            if (customerAndTransaction.phone.isNullOrEmpty()) {
                phoneNumber.gone()
            } else {
                phoneNumber.visible()
                phoneNumber.text = customerAndTransaction.phone
            }

            cbCustomer.isChecked = customerAndTransaction.isCheckedBoxChecked
            if (customerAndTransaction.name.isNotNullOrBlank()) {
                customerName.visible()
            } else {
                customerName.invisible()
            }
            customerName.text = customerAndTransaction.name

            customerAndTransaction.amount?.let {
                amountFinal.text =
                    context.getString(
                        R.string.amount_input,
                        TempCurrencyUtil.formatV2(it)
                    )
            }

            if (customerAndTransaction.error) {
                cbCustomer.isChecked = false
                cbCustomer.invisible()
                errorIv.visible()
                rootBg.setBackgroundColor(context.getColor(R.color.red_lite))
            } else {
                cbCustomer.visible()
                errorIv.gone()
                rootBg.setBackgroundColor(context.getColor(R.color.white))
            }

            if (cbCustomer.isChecked) {
                errorIv.gone()
            }

            customerAndTransaction.type?.let {
                if (customerAndTransaction.type == UserMigrationApiMessages.TRANSACTION.CREDIT.type) {
                    amountStatus.text = context.getString(R.string.due)
                    amountFinal.setTextColor(ContextCompat.getColor(context, R.color.red_1))
                } else {
                    amountStatus.text = context.getString(R.string.advance)
                    amountFinal.setTextColor(ContextCompat.getColor(context, R.color.green_primary))
                }
            }
        }
        this.customerAndTransaction = customerAndTransaction
    }

    interface ItemViewCustomerListener {
        fun showEditDetailsDialog(customer: CustomerUiTemplate)
        fun removeCustomer(customer: CustomerUiTemplate)
        fun showSolveErrorMessage()
    }

    @CallbackProp
    fun setListener(listener: ItemViewCustomerListener?) {
        this.listener = listener
    }
}
