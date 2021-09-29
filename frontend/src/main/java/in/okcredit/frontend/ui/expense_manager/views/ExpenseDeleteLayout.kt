package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.expense.models.Models
import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.expense_delete_layout.view.*
import tech.okcredit.android.base.utils.DateTimeUtils
import kotlin.math.roundToInt

class ExpenseDeleteLayout(context: Context?, attr: AttributeSet?) : LinearLayout(context, attr) {

    private var listener: Listener? = null
    init {
        View.inflate(context, R.layout.expense_delete_layout, this)
        root.setOnClickListener {
            listener?.onDismiss()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setContent(expense: Models.Expense) {
        type.text = expense.type
        if (expense.amount.minus(expense.amount.roundToInt()) == 0.0) {
            amount.text = expense.amount.toInt().toString()
        } else {
            amount.text = expense.amount.toString()
        }
        date.text = DateTimeUtils.getFormat2(context, expense.expenseDate)
        delete_container.setOnClickListener {
            listener?.onDeleteClicked(expense)
        }
    }

    interface Listener {
        fun onDeleteClicked(expense: Models.Expense)
        fun onDismiss()
    }
}
