package `in`.okcredit.merchant.customer_ui.ui.subscription.list

import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.SubscriptionItem
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SubscriptionListContract {

    data class State(
        val loading: Boolean = false,
        val list: List<SubscriptionItem>? = null,
        val customerName: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SubscriptionList(val list: List<Subscription>) : PartialState()

        data class SetCustomerName(val name: String) : PartialState()

        object NoChange : PartialState()

        object ShowProgress : PartialState()

        object ShowNetworkError : PartialState()

        object ShowServerError : PartialState()
    }

    sealed class Intent : UserIntent {
        data class ItemClicked(val id: String) : Intent()

        object Load : Intent()

        object AddSubscriptionClicked : Intent()

        object RefreshData : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object NetworkErrorToast : ViewEvent()

        object ServerErrorToast : ViewEvent()

        data class GoToAddSubscription(val customerId: String?, val customerMobile: String?) : ViewEvent()

        data class GoToSubscriptionDetail(val id: String, val customerId: String?, val subscription: Subscription?) :
            ViewEvent()
    }
}
