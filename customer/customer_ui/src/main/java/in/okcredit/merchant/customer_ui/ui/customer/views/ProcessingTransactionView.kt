package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.CustomerFragmentProcessingTxItemViewBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenItem
import `in`.okcredit.shared.performance.PerformanceTracker
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ProcessingTransactionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private var listener: Listener? = null
    private lateinit var txnData: CustomerScreenItem.ProcessingTransaction
    private var accountingEventTracker: AccountingEventTracker? = null

    private val binding: CustomerFragmentProcessingTxItemViewBinding =
        CustomerFragmentProcessingTxItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.textChatWithUs.debounceClickListener {
            listener?.chatWithUs(
                amount = txnData.amount,
                paymentTime = txnData.dateTime,
                txnId = txnData.paymentId,
                status = "Pending"
            )
        }

        binding.tvAddBankDetails.debounceClickListener {
            listener?.onAddBankDetailsClicked()
        }

        binding.tvCompleteKyc.debounceClickListener {
            listener?.completeKyc()
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPerformanceTracker(performanceTracker: PerformanceTracker) {
        binding.txContainer.setTracker { performanceTracker }
    }

    @ModelProp
    fun setProcessingTransaction(processingViewData: CustomerScreenItem.ProcessingTransaction) {
        txnData = processingViewData
    }

    @AfterPropsSet
    fun setDataAfterPropSet() {
        setContainerGravity(txnData.txnGravity)
        binding.processingTxDate.text = txnData.date
        binding.processingPaymentTitle.text = txnData.statusTitle
        binding.processingTxNote.text = txnData.statusNote
        setAmountUi(txnData)
        setProcessingAction()
    }

    private fun setProcessingAction() {
        when (txnData.action) {
            CustomerScreenItem.ProcessingTransactionAction.ADD_BANK -> {
                binding.tvAddBankDetails.visible()
                binding.tvCompleteKyc.gone()
                binding.textChatWithUs.gone()
            }
            CustomerScreenItem.ProcessingTransactionAction.HELP -> {
                binding.tvAddBankDetails.gone()
                binding.tvCompleteKyc.gone()
                binding.textChatWithUs.visible()
            }
            CustomerScreenItem.ProcessingTransactionAction.KYC -> {
                binding.tvAddBankDetails.gone()
                binding.tvCompleteKyc.visible()
                binding.textChatWithUs.gone()
            }
            else -> {
                binding.tvAddBankDetails.gone()
                binding.tvCompleteKyc.gone()
                binding.textChatWithUs.gone()
            }
        }
    }

    private fun setContainerGravity(gravity: TxnGravity) {
        binding.apply {
            dueContianer.updateLayoutParams<LinearLayout.LayoutParams> {
                this.gravity = if (gravity == TxnGravity.LEFT) {
                    Gravity.START
                } else {
                    Gravity.END
                }
            }

            txContainer.updateLayoutParams<LayoutParams> {
                this.gravity = if (gravity == TxnGravity.LEFT) {
                    Gravity.START
                } else {
                    Gravity.END
                }
            }
        }
    }

    private fun setAmountUi(processingViewData: CustomerScreenItem.ProcessingTransaction) {
        binding.apply {
            CurrencyUtil.renderV2(
                processingViewData.amount,
                processingTxAmount,
                processingViewData.txnGravity == TxnGravity.LEFT
            )

            CurrencyUtil.renderArrowsV3(
                processingViewData.amount,
                ivArrow,
                processingViewData.txnGravity == TxnGravity.LEFT
            )

            totalAmount.text = when {
                (processingViewData.currentBalance * 100).toInt() == 0 ->
                    "₹0 ${getString(R.string.due)}"
                processingViewData.currentBalance > 0 ->
                    "₹${CurrencyUtil.formatV2(processingViewData.currentBalance)} ${getString(R.string.due)}"
                else ->
                    "₹${CurrencyUtil.formatV2(processingViewData.currentBalance)} ${getString(R.string.advance)}"
            }
        }
    }

    interface Listener {
        fun completeKyc()
        fun onAddBankDetailsClicked()
        fun chatWithUs(amount: Long, paymentTime: String, txnId: String, status: String)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setTracker(tracker: AccountingEventTracker) {
        this.accountingEventTracker = tracker
    }
}
