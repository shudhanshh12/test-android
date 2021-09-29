package `in`.okcredit.merchant.customer_ui.ui.due_customer

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface DueCustomerContract {

    data class State(
        val isLoading: Boolean = false,

        val selectAll: Boolean = false,

        val isEmpty: Boolean = false,

        val isError: Boolean = false,

        val isNetworkError: Boolean = false,

        val isButtonEnabled: Boolean = false,

        val searchQuery: String = "",

        val dueCustomers: List<Customer> = mutableListOf(),

        val selectedCustomerIds: MutableList<String> = mutableListOf()

    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object ShowLoading : PartialState()

        object IsEmpty : PartialState()

        object IsError : PartialState()

        object IsNetworkError : PartialState()

        object IsButtonEnabled : PartialState()

        object NoChange : PartialState()

        data class SearchQuery(val searchQuery: String) : PartialState()

        data class SetDueCustomerList(val dueCustomers: List<Customer>) : PartialState()

        data class SelectCustomersIds(val selectedCustomerIds: MutableList<String>) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        data class SendReminders(val selectedCustomerIds: MutableList<String>) : Intent()

        object NoChange : Intent()

        object NotNow : Intent()

        data class SearchQuery(val searchQuery: String) : Intent()

        data class SelectAll(val selectAll: Boolean) : Intent()

        data class SelectItem(val customerId: String, val selectedCustomerIds: MutableList<String>) : Intent()
    }

    interface Navigator {
        fun goToMerchantDestinationpage(
            sourceScreen: String?,
            rewardsAmount: Long?,
            redirectToRewardsPage: Boolean? = false
        )
    }

    companion object {
        const val ARG_SOURCE = "source"
        const val ARG_REWARDS_AMOUNT = "rewards_amount"
        const val ARG_REDIRECT_TO_REWARDS_PAGE = "redirect_to_rewards_page"
    }
}
