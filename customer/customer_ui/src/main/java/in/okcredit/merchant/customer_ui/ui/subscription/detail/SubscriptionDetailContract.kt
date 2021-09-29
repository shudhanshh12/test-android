package `in`.okcredit.merchant.customer_ui.ui.subscription.detail

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface SubscriptionDetailContract {
    data class State(
        val customer: Customer? = null,
        val amount: Long = 0L,
        val name: String = "",
        val frequency: SubscriptionFrequency? = null,
        val startDate: Long = 0L,
        val daysInWeek: List<DayOfWeek>? = null,
        val nexDate: Long? = null,
        val txnCountSoFar: Int = 0,
        val status: SubscriptionStatus? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SubscriptionDetail(val subscription: Subscription) : PartialState()

        data class SetCustomerDetail(val customer: Customer) : PartialState()

        object NoChange : PartialState()

        object ShowProgress : PartialState()

        object SubscriptionDeleted : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object DeleteSubscription : Intent()

        object DeleteConfirmed : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class ShowError(@StringRes val error: Int) : ViewEvent()

        data class CustomerLoaded(val customerId: String, val mobile: String?) : ViewEvent()

        object ShowDeleteConfirm : ViewEvent()

        object HideDeleteLoader : ViewEvent()

        object ShowDeleteLoader : ViewEvent()
    }

    companion object {
        const val ARG_SUBSCRIPTION_ID = "arg_subscription_id"
        const val ARG_CUSTOMER_ID = "arg_customer_id"
        const val ARG_SOURCE = "arg_source"
        const val ARG_SUBSCRIPTION_OBJECT = "arg_subscription_object"
    }
}
