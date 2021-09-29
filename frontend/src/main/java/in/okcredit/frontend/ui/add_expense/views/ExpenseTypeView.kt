package `in`.okcredit.frontend.ui.add_expense.views

import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.item_expense_type.view.*

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ExpenseTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_expense_type, this, true)
    }

    var type: String = ""

    @ModelProp
    fun setExpense(expenseType: String?) {

        expense_type.text = expenseType

        type = expenseType ?: ""
    }

    interface ExpenseTypeViewClick {
        fun onExpenseClicked(expenseType: String)
    }

    @CallbackProp
    fun onExpandIconClick(listener: ExpenseTypeViewClick?) {
        expense_type.setOnClickListener {
            listener?.onExpenseClicked(type)
        }
    }
}
