package `in`.okcredit.frontend.ui.add_expense

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import org.joda.time.DateTime

interface AddExpenseContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val merchantAlreadyExistsError: Boolean = false,
        val userExpenseTypes: List<String?>? = null,
        val suggestions: List<String?>? = null,
        val date: DateTime = CommonUtils.currentDateTime(),
        val hideDateTag: Boolean = false,
        val showSubmitCTA: Boolean = false,
        val isFirstTransaction: Boolean = false,
        val canShowHandEducation: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetLoaderStatus(val status: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        object SetMerchantExists : PartialState()

        data class UserExpenseTypes(val expenseTypes: List<String>?) : PartialState()

        data class ChangeDate(val value: DateTime) : PartialState()

        data class HideDateTag(val hide: Boolean) : PartialState()

        data class ShowSuggestions(val list: List<String?>?) : PartialState()

        data class ShowSubmitCTA(val canShow: Boolean) : PartialState()

        data class SetFirstTransaction(val isFirstTransaction: Boolean) : PartialState()

        data class ShowHandEducation(val canShow: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class SubmitExpense(val expense: AddExpenseViewModel.AddExpense) : Intent()

        object ShowDatePickerDialog : Intent()

        // change bill_date
        data class OnChangeDate(val date: DateTime) : Intent()

        data class ShowSuggestions(val list: List<String?>?) : Intent()

        data class ShowSubmitCTA(val canShow: Boolean) : Intent()

        data class ShowHandEducationIntent(val canShow: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowDatePickerDialog : ViewEvent()
        object GoBack : ViewEvent()
    }
}
