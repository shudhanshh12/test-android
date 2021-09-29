package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.usecase.CreateStaffCollectionLink
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerWithPaymentDue
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface StaffLinkAddCustomerContract {
    data class State(
        val linkId: String? = null,
        val link: String? = null,
        val linkCreateTime: Long = 0L,
        val loading: Boolean = true,
        val totalDue: Long = 0L,
        val searchQuery: String = "",
        val showNoCustomerMessage: Boolean = true,
        val showEmptySearchResult: Boolean = false,
        val showEditableSearch: Boolean = false,
        val showSelectAllHeader: Boolean = true,
        val showTopSummaryCard: Boolean = true,
        val showBottomActions: Boolean = false,
        val selectedCustomerIds: Set<String> = emptySet(),
        val filteredCustomerList: List<CustomerItem> = emptyList(),
        val originalCustomerList: List<Customer> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object SearchClicked : PartialState()

        object DismissSearch : PartialState()

        data class SetPreSelectedCustomer(
            val list: List<Customer>,
        ) : PartialState()

        data class SetCustomerList(
            val customerSearchWrapper: GetCustomerWithPaymentDue.CustomerSearchWrapper,
        ) : PartialState()

        data class SetLoading(val loading: Boolean) : PartialState()

        data class SelectCustomers(val selectedCustomers: Set<String>) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class GetCustomersFromIds(val customerIds: List<String>) : Intent()

        data class SearchCustomer(val query: String) : Intent()

        object SearchClicked : Intent()

        object DismissSearch : Intent()

        object SelectAllCustomers : Intent()

        object DeselectAllCustomers : Intent()

        data class CustomerTapped(val id: String) : Intent()

        object ShareClicked : Intent()

        object AddDetailsClicked : Intent()
        object GoBack : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowSetUpCollection : ViewEvent()

        data class ShareOnWhatsApp(val staffLinkSummary: CreateStaffCollectionLink.StaffLinkSummary) : ViewEvent()

        data class MoveToAddDetails(val staffLinkSummary: CreateStaffCollectionLink.StaffLinkSummary) : ViewEvent()

        data class ShowError(@StringRes val error: Int) : ViewEvent()
    }

    companion object {
        const val ARG_LINK = "link"
        const val ARG_LINK_ID = "link_id"
        const val ARG_LINK_CREATE_TIME: String = "link_create_time"
        const val ARG_SELECTED_CUSTOMERS = "selected_customers"
    }
}
