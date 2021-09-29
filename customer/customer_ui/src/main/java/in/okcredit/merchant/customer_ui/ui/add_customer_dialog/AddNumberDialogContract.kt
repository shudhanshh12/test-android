package `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddNumberDialogContract {

    enum class AddNumberErrorType {
        MobileConflict,
        ActiveCyclicAccount,
        DeletedCyclicAccount,
        InternetIssue,
        SomeErrorOccurred
    }

    data class State(
        val enteredMobileNumber: String = "",
        val hasFocus: Boolean = false,
        val showLoader: Boolean = false,
        val error: Boolean = false,
        val errorPopup: Boolean = false,
        val errorType: AddNumberErrorType? = null,
        val customerId: String = "",
        val description: String = "",
        val mobile: String? = null,
        val errorCustomer: Customer? = null,
        val errorSupplier: Supplier? = null,
        val isSkipAndSend: Boolean = false,
        val screen: String = CustomerEventTracker.RELATIONSHIP_REMINDER,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class ShowPopupError(
            val errorPopup: Boolean,
            val errorType: AddNumberErrorType,
            val errorSupplier: Supplier? = null,
            val errorCustomer: Customer? = null,
        ) : PartialState()

        data class ShowError(
            val error: Boolean,
            val errorType: AddNumberErrorType,
            val errorCustomer: Customer? = null,
        ) : PartialState()

        data class EnteredMobileNumber(val enteredMobileNumber: String) : PartialState()

        data class SetEditTextFocus(val hasFocus: Boolean) : PartialState()

        data class ShowLoader(val showLoader: Boolean) : PartialState()

        data class SetBundleArguments(
            val customerId: String,
            val description: String,
            val mobile: String?,
            val isSkipAndSend: Boolean = false,
            val screen: String,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class EnteredMobileNumber(val enteredMobileNumber: String) : Intent()

        data class SubmitMobileNumber(val moblieNumber: String) : Intent()

        data class SetEditTextFocus(val hasFocus: Boolean) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GoToLogin : ViewEvents()

        object OnAccountAddedSuccessfully : ViewEvents()
    }
}
