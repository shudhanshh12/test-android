package `in`.okcredit.merchant.customer_ui.ui.transaction_details.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.core.model.History
import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.transaction_amount_history_item_view.view.*
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TransactionAmountHistoryView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.transaction_amount_history_item_view, this, true)
    }

    @ModelProp
    fun setTxnHistory(pair: Pair<Boolean, History>) {
        val isFirstEntry = pair.first
        val history = pair.second
        if (isFirstEntry) {
            tv_txn_type.text = getString(R.string.amount)
        } else {
            tv_txn_type.text = getString(R.string.edited_to)
        }
        tv_edited_on_date.text = DateTimeUtils.formatLong(DateTime(history.createdAt?.epoch))
        history.newAmount?.let {
            tv_edited_amount.text = context.getString(R.string.rupee_symbol) + CurrencyUtil.formatV2(it)
        }
    }
}
