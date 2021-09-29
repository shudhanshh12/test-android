package `in`.okcredit.frontend.ui.live_sales.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.utils.DrawableUtil
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.google.common.base.Strings
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.live_sales_tx_item_view.view.*
import kotlinx.android.synthetic.main.transaction_item_live_sale.view.*
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TransactionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var transaction: Transaction

    interface Listener {
        fun onTransactionClicked(collectionId: String, currentDue: Long)
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.live_sales_tx_item_view, this, true)
    }

    @ModelProp
    fun setData(transactionViewDataModel: TransactionViewDataModel) {
        this.transaction = transactionViewDataModel.transaction
        val paramsTxContainer = tx_container.layoutParams as FrameLayout.LayoutParams

        if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
            paramsTxContainer.gravity = Gravity.START
        } else if (transaction.type == Transaction.CREDIT) {
            paramsTxContainer.gravity = Gravity.END
        }
        tx_container.layoutParams = paramsTxContainer
        tx_container.requestLayout()

        tx_amount.text = String.format("₹%s", CurrencyUtil.formatV2(transaction.amountV2))
        if (transaction.billDate.withTimeAtStartOfDay() == transaction.createdAt.withTimeAtStartOfDay()) {
            tx_date.text = DateTimeUtils.formatTimeOnly(transaction.billDate)
        } else {
            tx_date.text = DateTimeUtils.formatDateOnly(transaction.billDate)
        }

        if (Strings.isNullOrEmpty(transaction.note)) {
            tx_note.visibility = View.GONE
        } else {
            tx_note.visibility = View.VISIBLE
            tx_note.text = transaction.note
        }

        if (transaction.isDirty) {
            sync.setImageDrawable(
                DrawableUtil.getDrawableWithColor(
                    context,
                    R.drawable.ic_sync_pending,
                    R.color.grey400
                )
            )
        } else {
            sync.setImageDrawable(
                DrawableUtil.getDrawableWithColor(
                    context,
                    `in`.okcredit.merchant.customer_ui.R.drawable.ic_single_tick,
                    R.color.grey400
                )
            )
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        tx_container.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onTransactionClicked(transaction.id, transaction.currentDue) }
            .subscribe()
    }

    data class TransactionViewDataModel(
        val transaction: Transaction,
    )
}
