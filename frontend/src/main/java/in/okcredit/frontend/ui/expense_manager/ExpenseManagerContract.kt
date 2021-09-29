package `in`.okcredit.frontend.ui.expense_manager

import `in`.okcredit.expense.models.Models
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope

interface ExpenseManagerContract {

    companion object {
        const val DEFAULT_EXPENSE_FILTER = "expense_filter"
    }

    enum class OnBoardingVariant {
        v1, v2, v3
    }

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val list: List<Models.Expense>? = null,
        val totalAmount: Double = 0.0,
        val filter: Filter = Filter.ALL,
        val startDate: DateTime? = null,
        val endDate: DateTime? = null,
        val isThisMonthDefault: Boolean = false,
        val showAddexpense: Boolean = false,
        val showVideoInitial: Boolean = false,
        val videoUrl: String = "7f2kSvERoQI",
        val isFirstTime: Boolean = false,
        val isSummaryViewAbEnabled: Boolean = false,
        val isNewUser: Boolean = false,
        val canShowAddExpenseEducation: Boolean = false,
        val canShowInfoGraphic: Boolean = false,
        val scrollToTop: Boolean = false,
        val canShowDeleteLayout: Boolean = false,
        val canShowDeleteConfirmDialog: Boolean = false,
        val selectedExpense: Models.Expense? = null,
        val onBoardingVariant: OnBoardingVariant = OnBoardingVariant.v1,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : ExpenseManagerContract.PartialState()

        object ErrorState : ExpenseManagerContract.PartialState()

        data class ShowAlert(val message: String) : ExpenseManagerContract.PartialState()

        object HideAlert : ExpenseManagerContract.PartialState()

        object NoChange : ExpenseManagerContract.PartialState()

        data class SetNetworkError(val networkError: Boolean) : ExpenseManagerContract.PartialState()

        data class SetLoaderStatus(val status: Boolean) : ExpenseManagerContract.PartialState()

        object ClearNetworkError : ExpenseManagerContract.PartialState()

        data class SetAllExpenses(val expenseResponse: Models.ExpenseListResponse) : PartialState()

        data class SetExpenses(val expenseResponse: Models.ExpenseListResponse) : PartialState()

        data class showAddExpense(val canShow: Boolean) : PartialState()

//        data class showVideoInitial(val showVideoInitial : Boolean) : PartialState()

        data class ChangeFilter(val filter: Filter) : PartialState()

        data class showSummaryViewAb(val showSummaryViewAb: Boolean) : PartialState()

        data class SetNewUser(val isNewUser: Boolean) : PartialState()

        data class SetFirstAddExpenseEducation(val canShow: Boolean) : PartialState()

//        data class ShowInfoGraphicAb(val canShow: Boolean) : PartialState()

        data class ShowDeleteLayout(val expense: Models.Expense) : PartialState()

        object HideDeleteLayout : PartialState()

        data class ShowDeleteConfirmDialog(val canShow: Boolean) : PartialState()

        data class SetOnBoardingVariant(val onBoardingVariant: OnBoardingVariant) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object Retry : Intent()

        object GetAllExpenses : Intent()

        data class GetExpenses(val startDate: DateTime, val endDate: DateTime) : Intent()

        data class SetDateRangeIntent(val startDate: DateTime, val endDate: DateTime) : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class DeleteExpense(val id: String) : Intent()

        data class ChangeFilter(val filter: Filter) : Intent()

        data class SetFirstAddExpenseEducation(val canShow: Boolean) : Intent()

        data class RxPreferenceBoolean(val key: String, val value: Boolean, val scope: Scope) : Intent()

        data class RxPreferenceString(val key: String, val value: String, val scope: Scope) : Intent()

        object OnAddExpenseClicked : Intent()

        data class ShowDeleteLayout(val expense: Models.Expense) : Intent()

        object HideDeleteLayout : Intent()

        data class ShowDeleteConfirmDialog(val canShow: Boolean) : Intent()

        data class SubmitFeedBack(val msg: String) : Intent()
    }

    interface Navigator {

        fun showAll()
        fun showToday()
        fun showThisMonth()
        fun showLastMonth()
        fun showForSelectedRange(startDate: DateTime, endDate: DateTime)
        fun reLoad()
        fun gotoLogin()
        fun goToAddExpenseScreen()
        fun trackEventOnLoad(isNewUser: Boolean, isInfoGraphicShown: Boolean)
    }

    sealed class ViewEvent : BaseViewEvent

    enum class Filter {
        ALL, TODAY, THIS_MONTH, LAST_MONTH, DATE_RANGE
    }
}
