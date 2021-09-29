package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerContract
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.item_summary_view.view.*
import kotlinx.android.synthetic.main.item_summary_view_ab.view.*
import kotlinx.android.synthetic.main.item_summary_view_ab.view.date_range
import kotlinx.android.synthetic.main.item_summary_view_ab.view.total_expense
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.*
import kotlin.math.roundToInt

class SummaryViewAB(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_summary_view_ab, this, true)
    }

    fun setExpense(state: ExpenseManagerContract.State) {
        state.totalAmount?.let {
            if (it.minus(it.roundToInt()) == 0.0) {
                total_expense.text = String.format(Locale.ENGLISH, "%d", it.toInt())
            } else {
                total_expense.text = String.format(Locale.ENGLISH, "%.2f", it.toString().toDouble())
            }
        }
        if (state.startDate != null && state.endDate != null) {
            date_range.text = DateTimeUtils.getFormat1(state.startDate) + " - " + DateTimeUtils.getFormat1(state.endDate)
        }
    }
}
