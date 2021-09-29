package `in`.okcredit.frontend.ui.supplier.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.SupplierFragmentProcessingTxItemViewBinding
import `in`.okcredit.frontend.ui.supplier.SupplierScreenItem
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.utils.AccountingSharedUtils.getWhatsAppMsg
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SupplierProcessingTransactionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private var listener: Listener? = null
    private lateinit var processingTxn: SupplierScreenItem.ProcessingTransaction
    private var accountingEventTracker: AccountingEventTracker? = null

    private val binding: SupplierFragmentProcessingTxItemViewBinding =
        SupplierFragmentProcessingTxItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.textChatWithUs.setOnClickListener {
            accountingEventTracker?.trackCustomerSupportMsgClicked(
                source = "ledger",
                txnId = processingTxn.paymentId,
                amount = processingTxn.amount.toString(),
                relation = LedgerType.SUPPLIER.value.lowercase(),
                status = "pending",
                supportMsg = getWhatsAppMsg(
                    context,
                    amount = CurrencyUtil.formatV2(processingTxn.amount),
                    paymentTime = DateTimeUtils.formatLong(processingTxn.billDate),
                    txnId = processingTxn.paymentId,
                    status = "pending",
                ),
                type = processingTxn.supportType,
            )
            listener?.chatWithUs(
                amount = processingTxn.amount.toString(),
                paymentTime = DateTimeUtils.formatLong(processingTxn.billDate),
                txnId = processingTxn.paymentId,
                status = "Pending"
            )
        }
    }

    @ModelProp
    fun setProcessingTransaction(transaction: SupplierScreenItem.ProcessingTransaction) {
        processingTxn = transaction
    }

    @AfterPropsSet
    fun setDataAfterPropSet() {
        binding.apply {
            CurrencyUtil.renderV2(processingTxn.amount, processingTxAmount, processingTxn.payment)
            if (processingTxn.billDate.withTimeAtStartOfDay() == processingTxn.createTime.withTimeAtStartOfDay()) {
                processingTxDate.text = DateTimeUtils.formatTimeOnly(processingTxn.billDate)
            } else {
                processingTxDate.text = DateTimeUtils.formatDateOnly(processingTxn.billDate)
            }
            val paramsTxContainer = txContainer.layoutParams as LayoutParams

            val paramsBottomContainer = dueContianer.layoutParams as LinearLayout.LayoutParams

            if (processingTxn.payment.not()) {
                paramsTxContainer.gravity = Gravity.START
                paramsBottomContainer.gravity = Gravity.START
            } else {
                paramsTxContainer.gravity = Gravity.END
                paramsBottomContainer.gravity = Gravity.END
            }

            if (processingTxn.isBlindPay) {
                processingTxNote.text = context.getString(R.string.blind_pay_payment_processing_supplier)
            } else {
                processingTxNote.text = context.getString(R.string.processing_tx_description)
            }

            CurrencyUtil.renderArrowsV2(processingTxn.amount, ivArrow, processingTxn.payment)
            txContainer.layoutParams = paramsTxContainer
            txContainer.requestLayout()

            when {
                (processingTxn.currentDue * 100).toInt() == 0 ->
                    "₹0 ${context.resources.getString(R.string.due)}".also { totalAmount.text = it }
                processingTxn.currentDue > 0 ->
                    "₹${CurrencyUtil.formatV2(processingTxn.currentDue)} ${context.resources.getString(R.string.due)}".also {
                        totalAmount.text = it
                    }
                else ->
                    "₹${CurrencyUtil.formatV2(processingTxn.currentDue)} ${context.resources.getString(R.string.advance)}".also {
                        totalAmount.text = it
                    }
            }
        }
        setHelpUi()
    }

    private fun setHelpUi() {
        if (processingTxn.shouldShowHelpOption) {
            accountingEventTracker?.trackCustomerSupportLedgerMsgShown(
                accountId = processingTxn.accountId,
                txnId = processingTxn.paymentId,
                amount = processingTxn.amount.toString(),
                relation = LedgerType.SUPPLIER.value.lowercase(),
                status = "pending",
                supportMsg = context.getString(R.string.t_002_24X7help_helpbox_CTA)

            )
            binding.textChatWithUs.visible()
        } else binding.textChatWithUs.gone()
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setTracker(tracker: AccountingEventTracker) {
        this.accountingEventTracker = tracker
    }

    interface Listener {
        fun chatWithUs(amount: String, paymentTime: String, txnId: String, status: String)
    }
}
