package `in`.okcredit.sales_ui.ui.billing_name

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.contacts.contract.model.Contact

interface BillingNameContract {
    data class State(
        val isPermissionGranted: Boolean = false,
        val contacts: List<Contact>? = null,
        val canShowMobileField: Boolean = false,
        val name: String = "",
        val mobile: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetContactsData(val contacts: List<Contact>, val isPermissionGranted: Boolean) : PartialState()

        data class SetContactsDataWithName(val contacts: List<Contact>, val isPermissionGranted: Boolean, val name: String = "") : PartialState()

        data class SetContactsDataWithMobile(val contacts: List<Contact>, val isPermissionGranted: Boolean, val mobile: String = "") : PartialState()

        object ShowMobileField : PartialState()

        data class SetName(val name: String) : PartialState()

        data class SetMobile(val mobile: String) : PartialState()

        data class SetData(val name: String, val mobile: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class GetContactsIntent(val name: String) : Intent()

        data class RefreshContactIntent(val refresh: Boolean, val searchQuery: String) : Intent()

        object GetContactPermissionIntent : Intent()

        object ShowMobileFieldIntent : Intent()

        data class SubmitIntent(val name: String, val mobile: String) : Intent()

        object ValidationErrorIntent : Intent()

        data class SetNameIntent(val name: String) : Intent()

        data class SetMobileIntent(val mobile: String) : Intent()

        data class SetDataIntent(val name: String, val mobile: String) : Intent()
    }

    interface Navigator {
        fun onSubmit(name: String, mobile: String)
        fun getContactPermission()
        fun showMobileField()
    }
}
