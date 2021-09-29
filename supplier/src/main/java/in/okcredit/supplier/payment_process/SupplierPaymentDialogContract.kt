package `in`.okcredit.supplier.payment_process

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.CUSTOMER
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.CUSTOMER_SCREEN
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierPropertyValue.SUPPLIER_SCREEN
import merchant.okcredit.accounting.contract.model.LedgerType

interface SupplierPaymentDialogContract {

    data class State(
        val isLoading: Boolean = false,
        val accountId: String = "",
        val mobile: String = "",
        val balance: Long = 0L,
        val destinationType: String = "",
        val messageLink: String? = "",
        val paymentAddress: String = "",
        val name: String = "",
        val accountType: String = ""
    ) : UiState {
        fun getRelationFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) SUPPLIER
            else CUSTOMER
        }

        fun getScreenFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) SUPPLIER_SCREEN
            else CUSTOMER_SCREEN
        }
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object OnConfirmClicked : Intent()
        object OnChangeDetails : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object OnConfirmClicked : ViewEvent()
        object OnChangeDetails : ViewEvent()
    }
}
