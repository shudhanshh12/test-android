package `in`.okcredit.merchant.customer_ui.ui.staff_link.edit

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.data.server.model.response.ActiveStaffLinkResponse
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface StaffLinkEditDetailsContract {

    data class State(
        val linkId: String = "",
        val link: String? = null,
        val linkCreateTime: Long? = null,
        val loading: Boolean = true,
        val totalDue: Long = 0L,
        val customerCountWithBalanceDue: Int = 0,
        val customerList: List<StaffLinkEditDetailsCustomerItem> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetLinkDetails(val details: ActiveStaffLinkResponse) : PartialState()

        data class SetCustomers(val list: List<Customer>) : PartialState()

        data class CustomersDeleted(val list: List<String>) : PartialState()

        object NoChange : PartialState()

        data class SetLoading(val loading: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object LoadLinkDetails : Intent()

        data class GetCustomersFromIds(val customerIds: List<String>) : Intent()

        object DeleteLinkClicked : Intent()

        object DeleteLinkConfirm : Intent()

        data class DeleteCustomerClicked(val customerId: String) : Intent()

        data class DeleteCustomerConfirmed(val customerId: String) : Intent()

        data class UpdateCustomerMobileClicked(val customerId: String) : Intent()

        data class UpdateCustomerAddressClicked(val customerId: String) : Intent()

        data class DeleteCustomerCancelled(val customerId: String) : Intent()

        object ShareClicked : Intent()

        object AddToListClicked : Intent()

        object DeleteLinkCancelled : Intent()

        object GoBack : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object FinishScreen : ViewEvent()

        data class ShowConfirmDelete(val customerId: String? = null) : ViewEvent()

        data class ShowUpdateMobile(val customerId: String, val mobile: String?) : ViewEvent()

        data class ShowUpdateAddress(val customerId: String, val address: String?) : ViewEvent()

        data class ShareOnWhatsApp(val linkSummary: String) : ViewEvent()
        data class GoToAddCustomer(
            val linkId: String?,
            val link: String?,
            val linkCreateTime: Long?,
            val customerIds: List<String>,
        ) : ViewEvent()

        data class ShowError(@StringRes val error: Int) : ViewEvent()
    }

    companion object {
        const val ARG_LINK_CREATE_TIME: String = "link_create_time"
        const val ARG_LINK = "link"
        const val ARG_LINK_ID = "link_id"
        const val ARG_SELECTED_CUSTOMERS = "selected_customers"
    }
}
