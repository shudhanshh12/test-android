package `in`.okcredit.frontend.ui.expense_manager.views

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerContract
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerFragment
import android.annotation.SuppressLint
import com.airbnb.epoxy.AsyncEpoxyController
import javax.inject.Inject

class ExpenseController(private val fragment: ExpenseManagerFragment) : AsyncEpoxyController() {

    @Inject
    lateinit var tracker: Tracker

    private var states = ExpenseManagerContract.State()

    fun setStates(states: ExpenseManagerContract.State) {
        this.states = states
        requestModelBuild()
    }

    @SuppressLint("DefaultLocale")
    override fun buildModels() {
        states.let {
            it.list?.let { expenses ->

                val list = expenses.filter { item -> item.deletedAt == null }

                for (expense in list) {
                    expenseView {
                        id(expense.id)
                        expense(expense)
                        listener(fragment)
                    }
                }
                if (list.size == 1) {
                    expenseInfoGraphicView {
                        id("info")
                    }
                }
            }
        }
    }
}
