package `in`.okcredit.sales_ui.ui.view_sale

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime

interface SalesDetailContract {
    data class State(
        val isLoading: Boolean = false,
        val networkError: Boolean = false,
        val error: Boolean = false,
        val sale: Models.Sale? = null,
        val saleId: String = "",
        val amount: Double = 0.0,
        val date: DateTime? = null,
        val note: String = "",
        val canShowAlert: Boolean = false,
        val alert: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class ShowSaleDetail(val sale: Models.Sale) : PartialState()

        object NoChange : PartialState()

        data class ShowAlert(val msg: String) : PartialState()

        object HideAlert : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object ShowDeleteDialog : Intent()

        data class DeleteSale(val saleId: String) : Intent()
    }

    interface Navigator {
        fun gotoLogin()
        fun showDeleteDialog(saleId: String)
        fun onDeleted()
    }
}
