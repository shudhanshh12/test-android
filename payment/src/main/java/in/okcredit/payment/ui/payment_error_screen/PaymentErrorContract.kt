package `in`.okcredit.payment.ui.payment_error_screen

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.LedgerType

interface PaymentErrorContract {

    data class State(
        val errorType: PaymentErrorType = PaymentErrorType.OTHER,
        val errorMessage: String = "",
        val accountType: String = "",
    ) : UiState {
        fun getRelationFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) PaymentAnalyticsEvents.PaymentPropertyValue.SUPPLIER
            else PaymentAnalyticsEvents.PaymentPropertyValue.CUSTOMER
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetErrorType(val errorType: PaymentErrorType, val errorMsg: String = "") : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object OnRetry : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object OnRetry : ViewEvents()
    }
}
