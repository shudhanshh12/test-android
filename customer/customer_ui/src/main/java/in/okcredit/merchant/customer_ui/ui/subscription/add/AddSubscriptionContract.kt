package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface AddSubscriptionContract {

    data class State(
        val customer: Customer? = null,
        val amount: Long? = null,
        val amountCalculation: String? = null,
        val name: String? = null,
        val selectedFrequency: SubscriptionFrequency? = null,
        val daysInWeek: List<DayOfWeek>? = null,
        val startDate: Long? = null
    ) : UiState {

        fun shouldShowSaveButton() = amountAdded() && nameAdded()

        fun amountAdded() = amount != null && amount > 0

        fun nameAdded() = !name.isNullOrBlank()
    }

    sealed class PartialState : UiState.Partial<State> {
        data class CalculatorData(val amount: Long, val amountCalculation: String?) : PartialState()

        data class NameAdded(val name: String) : PartialState()

        data class FrequencyAdded(
            val selectedFrequency: SubscriptionFrequency,
            val daysInWeek: List<DayOfWeek>?,
            val startDate: Long?
        ) : PartialState()

        object NoChange : PartialState()

        data class CustomerData(val value: Customer?) : PartialState()

        object ShowProgress : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object AddNameClicked : Intent()

        object AddFrequencyClicked : Intent()

        object SubmitClicked : Intent()

        data class NameAdded(val name: String) : Intent()

        data class FrequencyAdded(
            val selectedFrequency: SubscriptionFrequency,
            val daysInWeek: List<DayOfWeek>?,
            val startDate: Long?
        ) : Intent()

        data class CalculatorData(val replace: String?, val amount: Long) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(@StringRes val error: Int) : ViewEvent()

        data class GoToAddName(val name: String?) : ViewEvent()

        data class GoToAddFrequency(
            val selectedFrequency: SubscriptionFrequency?,
            val daysInWeek: List<DayOfWeek>?,
            val startDate: Long?
        ) : ViewEvent()

        data class Success(val startDate: Long) : ViewEvent()
    }

    companion object {
        const val SUBSCRIPTION_ADDED: String = "SUBSCRIPTION_ADDED"
    }
}
