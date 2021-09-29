package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.expense.models.Models
import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.item_expense.view.*
import tech.okcredit.android.base.utils.DateTimeUtils
import kotlin.math.roundToInt

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ExpenseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listener: Listener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_expense, this, true)
    }

    @ModelProp
    fun setExpense(expense: Models.Expense) {
        if (expense.amount.minus(expense.amount.roundToInt()) == 0.0) {
            amount.text = expense.amount.toInt().toString()
        } else {
            amount.text = expense.amount.toString()
        }

        if (expense.deletedAt != null) {
            amount_layout.background = context.getDrawable(R.drawable.strike_through)
            deleted.visibility = View.VISIBLE
            amount.setTextColor(context.resources.getColor(R.color.grey800))
            type.setTextColor(context.resources.getColor(R.color.grey800))
            date.setTextColor(context.resources.getColor(R.color.grey600))
        } else {
            deleted.visibility = View.GONE
            amount_layout.background = null
            amount.setTextColor(context.resources.getColor(R.color.grey900))
            type.setTextColor(context.resources.getColor(R.color.grey900))
        }
        type.text = expense.type
        date.text = DateTimeUtils.getFormat2(context, expense.expenseDate)
        root_expense_type.setOnLongClickListener {
            if (expense.deletedAt == null) {
                listener?.onLongClick(expense)
            }
            return@setOnLongClickListener true
        }
        root_expense_type.setOnClickListener {
            listener?.onClick(expense)
        }
    }

    @CallbackProp
    fun setListener(listener: ExpenseView.Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onLongClick(expense: Models.Expense)
        fun onClick(expense: Models.Expense)
    }
}
