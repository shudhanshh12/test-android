package `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.utils.UserMigrationUtils.ErrorType
import java.util.*

interface EditDetailContract {
    data class State(
        val showLoading: Boolean = false,
        val customer: CustomerUiTemplate? = null,
        val selectedDate: Calendar = Calendar.getInstance(),
        val customerName: String = "",
        val phoneNumber: String = "",
        val billDate: String = "",
        val visibleAmount: String = "0",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object ShowLoading : PartialState()
        data class SelectBillDateAndUpdateEntries(
            val amount: String,
            val mobile: String,
            val name: String
        ) : PartialState()

        data class UpdateEntries(val customerName: String, val phoneNumber: String, val amount: String) : PartialState()
        data class SetCustomerData(val customer: CustomerUiTemplate?) : PartialState()
        data class BillDateAdded(val calendar: Calendar) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class SelectBillDateAndUpdateEntries(
            val amount: String,
            val mobile: String,
            val name: String
        ) : Intent()

        data class SetUpdatedEntries(
            val customerName: String,
            val mobile: String,
            val amount: Long,
        ) : Intent()

        data class CheckValidation(
            val customerName: String,
            val mobile: String,
            val amount: String,
        ) : Intent()

        data class BillDateSelected(val calendar: Calendar) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowCalendar(val selectedDate: Calendar?) : ViewEvent()
        data class ValidationErrors(val anyError: List<ErrorType>) : ViewEvent()
        data class SetUpdatedEntries(
            val customerName: String,
            val mobile: String,
            val amount: Long,
        ) : ViewEvent()
    }
}
