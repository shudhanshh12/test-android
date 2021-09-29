package `in`.okcredit.frontend.ui.add_expense.views

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.add_expense.AddExpenseContract
import `in`.okcredit.frontend.ui.add_expense.AddExpenseFragment
import com.airbnb.epoxy.AsyncEpoxyController
import java.util.*
import javax.inject.Inject

class ExpenseTypeController(private val fragment: AddExpenseFragment) : AsyncEpoxyController() {

    @Inject
    lateinit var tracker: Tracker

    private var states = AddExpenseContract.State()

    fun setStates(states: AddExpenseContract.State) {
        this.states = states
        requestModelBuild()
    }

    override fun buildModels() {
        states.let {
            it.suggestions?.let {
                var list = fragment.context?.resources?.getStringArray(R.array.default_expense_types)?.toList() as List<String?>?

                if (states.isFirstTransaction.not()) {
                    list = if (it.size > 3) it.subList(0, 3) else it
                } else {
                    list = if (list != null && list.size > 3) list.subList(0, 3) else listOf()
                }

                for (expenseType in list) {
                    expenseTypeView {
                        id(expenseType)
                        expense(expenseType)
                        onExpandIconClick(fragment)
                    }
                }
            }
        }
    }
}
