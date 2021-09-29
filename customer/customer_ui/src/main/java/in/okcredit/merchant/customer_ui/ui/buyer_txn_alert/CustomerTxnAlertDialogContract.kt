package `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CustomerTxnAlertDialogContract {

    data class State(
        val customerId: String? = null,
        val amount: Long = 0L,
        val name: String? = null,
        val mobile: String? = null,
        val profilePic: String? = null,
        val type: Int = -1
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetAmount(val amount: Long, val type: Int) : PartialState()

        data class SetCustomer(val mobile: String?, val description: String, val profilePic: String?, val customerId: String?) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object AllowAction : Intent()

        object DenyAction : Intent()

        object Dismiss : Intent()
    }

    interface Navigator {
        fun goToLogin()
        fun dismissDialog()
        fun showDenyPopUp()
        fun showAllowPopUp()
    }
}
