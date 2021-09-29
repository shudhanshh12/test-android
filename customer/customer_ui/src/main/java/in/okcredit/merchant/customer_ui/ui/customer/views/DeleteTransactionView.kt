package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenItem
import `in`.okcredit.shared.performance.PerformanceTracker
import `in`.okcredit.shared.utils.SharedDrawableUtils
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.contract.model.LedgerTxnStatus
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.databinding.TransactionDeleteViewBinding
import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity
import merchant.okcredit.accounting.utils.AccountingSharedUtils.ellipsizeName
import merchant.okcredit.accounting.utils.AccountingSharedUtils.getWhatsAppMsg
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DeleteTransactionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var txnItem: CustomerScreenItem.DeletedTransaction
    private var listener: Listener? = null
    private var accountingEventTracker: AccountingEventTracker? = null
    private var status: String = ""

    interface Listener {
        fun onTransactionClicked(txnId: String, currentDue: Long)
        fun chatWithUs(amount: String, paymentTime: String, txnId: String, status: String)
    }

    private val binding: TransactionDeleteViewBinding =
        TransactionDeleteViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        rootView.debounceClickListener {
            this.listener?.onTransactionClicked(txnItem.id, txnItem.currentBalance)
        }

        binding.textChatWithUs.debounceClickListener {
            accountingEventTracker?.trackCustomerSupportMsgClicked(
                source = "ledger",
                txnId = txnItem.paymentId,
                amount = txnItem.amount.toString(),
                relation = LedgerType.CUSTOMER.value.lowercase(),
                status = status.lowercase(),
                supportMsg = getWhatsAppMsg(
                    context,
                    amount = CurrencyUtil.formatV2(txnItem.amount),
                    paymentTime = txnItem.dateTime,
                    txnId = txnItem.paymentId,
                    status = this.status
                ),
                type = txnItem.supportType

            )
            listener?.chatWithUs(
                amount = txnItem.amount.toString(),
                paymentTime = txnItem.dateTime,
                txnId = txnItem.paymentId,
                status = status
            )
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setPerformanceTracker(performanceTracker: PerformanceTracker) {
        binding.txContainer.setTracker { performanceTracker }
    }

    @ModelProp
    fun setTransaction(deletedTransaction: CustomerScreenItem.DeletedTransaction) {
        txnItem = deletedTransaction
    }

    @AfterPropsSet
    fun setDataOncePropSet() {
        setGravity(txnItem.txnGravity)
        setTxnTagUi(txnItem)
        setBottomUi(txnItem)
    }

    private fun setBottomUi(deletedTransaction: CustomerScreenItem.DeletedTransaction) {
        if (deletedTransaction.onlineTxn) {
            setOnlineTxnUi(deletedTransaction)
        } else {
            setDeleteUi(deletedTransaction)
        }
    }

    private fun setOnlineTxnUi(deleteTransactionHelper: CustomerScreenItem.DeletedTransaction) {
        binding.apply {
            clRefund.visible()
            llDelete.gone()
            when (deleteTransactionHelper.collectionStatus) {
                CollectionStatus.REFUNDED -> {
                    tvReason.gone()
                    ivStatusEnd.visible()
                    tvStatus.text = getString(R.string.refund_successful)
                    ivStatus.setImageDrawable(context.getDrawableCompact(R.drawable.ic_refund))
                }
                CollectionStatus.REFUND_INITIATED -> {
                    setCustomerSupportUi(LedgerTxnStatus.REFUND_INITIATED.value)
                    ivStatusEnd.gone()
                    tvReason.visible()
                    tvReason.text = getString(R.string.payment_refund_reason_payout_initiated)
                    tvStatus.text = getString(R.string.refund_initiated)
                    ivStatus.setImageDrawable(context.getDrawableCompact(R.drawable.ic_refund))
                }
                else -> {
                    setCustomerSupportUi(LedgerTxnStatus.FAILED.value)
                    ivStatusEnd.gone()
                    tvReason.visible()
                    if (deleteTransactionHelper.isBlindPay) {
                        tvReason.text = getString(R.string.blind_pay_payment_failed_customer)
                    } else {
                        tvReason.text = getString(R.string.supplier_payment_pending)
                    }
                    tvStatus.text = getString(R.string.supplier_payment_failed)
                    ivStatus.setImageDrawable(context.getDrawableCompact(R.drawable.ic_error))
                }
            }

            CurrencyUtil.renderDeletedAmount(deleteTransactionHelper.amount, tvAmountDeleted)
            CurrencyUtil.renderArrowsV3(
                deleteTransactionHelper.amount,
                tvArrowsRefund,
                deleteTransactionHelper.txnGravity == TxnGravity.LEFT
            )
            tvDate.text = deleteTransactionHelper.date
        }
    }

    private fun setCustomerSupportUi(status: String) {
        this.status = status
        if (txnItem.shouldShowHelpOption) {
            accountingEventTracker?.trackCustomerSupportLedgerMsgShown(
                accountId = txnItem.accountId,
                txnId = txnItem.paymentId,
                amount = txnItem.amount.toString(),
                relation = LedgerType.CUSTOMER.value.lowercase(),
                status = status.lowercase(),
                supportMsg = context.getString(R.string.t_002_24X7help_helpbox_CTA)

            )
            binding.textChatWithUs.visible()
        } else binding.textChatWithUs.gone()
    }

    private fun setDeleteUiText(deletedTransaction: CustomerScreenItem.DeletedTransaction) {
        val deleteTextResourceId = if (deletedTransaction.onlineTxn) {
            when (deletedTransaction.collectionStatus) {
                CollectionStatus.FAILED -> {
                    if (isTxnPayment(deletedTransaction)) R.string.payment_incomplete else R.string.credit_incomplete
                }
                else -> {
                    if (isTxnPayment(deletedTransaction)) R.string.payment_failed else R.string.credit_failed
                }
            }
        } else {
            if (isTxnPayment(deletedTransaction)) R.string.payment_deleted else R.string.credit_deleted
        }

        (context.resources.getString(deleteTextResourceId) + " ").also {
            binding.deleteText.text = it
        } // This blank space is necessary to show full text
    }

    private fun isTxnPayment(transaction: CustomerScreenItem.DeletedTransaction) =
        transaction.txnGravity == TxnGravity.LEFT

    private fun setGravity(gravity: TxnGravity) {
        binding.txContainer.updateLayoutParams<LayoutParams> {
            this.gravity = if (gravity == TxnGravity.LEFT) {
                Gravity.START
            } else {
                Gravity.END
            }
        }
    }

    private fun setTxnTagUi(transaction: CustomerScreenItem.DeletedTransaction) {
        binding.apply {
            if (transaction.isDeletedByCustomer ||
                (transaction.onlineTxn && transaction.collectionStatus == CollectionStatus.FAILED)
            ) {
                txTag.visible()
                if (transaction.onlineTxn) {
                    txTag.text = context.getString(R.string.online_payment_transaction)
                } else {
                    txTag.text = String.format(
                        context.getString(R.string.deleted_by),
                        ellipsizeName(transaction.customerName)
                    )
                }
            } else {
                txTag.gone()
            }
        }
    }

    private fun setDirtyTxnUi(isDirty: Boolean) {
        binding.apply {
            if (isDirty) {
                sync.setImageDrawable(
                    SharedDrawableUtils.getDrawableWithColor(
                        context,
                        R.drawable.ic_sync_pending,
                        R.color.grey400
                    )
                )
            } else {
                sync.setImageDrawable(
                    SharedDrawableUtils.getDrawableWithColor(
                        context,
                        R.drawable.ic_single_tick,
                        R.color.grey400
                    )
                )
            }
        }
    }

    private fun setDeleteUi(deletedTransaction: CustomerScreenItem.DeletedTransaction) {
        binding.apply {
            llDelete.visible()
            clRefund.gone()
            textChatWithUs.gone()
            setDirtyTxnUi(deletedTransaction.isDirty)
            setDeleteUiText(deletedTransaction)
            deletedAmountLayout.visibility = View.VISIBLE
            CurrencyUtil.renderDeletedAmount(deletedTransaction.amount, amountDeleted)
            CurrencyUtil.renderArrowsV3(
                deletedTransaction.amount,
                arrows, deletedTransaction.txnGravity == TxnGravity.LEFT
            )
            arrows.imageTintList = (ColorStateList.valueOf(ContextCompat.getColor(arrows.context, R.color.grey500)))
            deletedIcon.setImageDrawable(
                SharedDrawableUtils.getDrawableWithColor(
                    context,
                    R.drawable.ic_delete_outline,
                    R.color.grey600
                )
            )
        }
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
