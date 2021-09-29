package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerContract
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.item_expense.view.*
import kotlinx.android.synthetic.main.item_summary_view.view.*
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.*
import kotlin.math.roundToInt

class SummaryView(context: Context?, attr: AttributeSet?) : LinearLayout(context, attr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_summary_view, this, true)
    }

    fun setExpense(state: ExpenseManagerContract.State) {
        if (state.totalAmount.minus(state.totalAmount.roundToInt()) == 0.0) {
            total_expense.text = String.format(Locale.ENGLISH, "%d", state.totalAmount.toInt())
        } else {
            total_expense.text = String.format(Locale.ENGLISH, "%.2f", state.totalAmount.toString().toDouble())
        }
        if (state.startDate != null && state.endDate != null) {
            date_range.text = DateTimeUtils.getFormat1(state.startDate) + " - " + DateTimeUtils.getFormat1(state.endDate)
        } else {
            date_range.text = "-"
        }
    }
}
