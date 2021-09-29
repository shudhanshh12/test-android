package `in`.okcredit.sales_ui.ui.bill_summary

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BillSummaryContract {

    data class State(
        val isEditable: Boolean = false,
        val isLoading: Boolean = false,
        val authToken: String? = null,
        val saleId: String? = null,
        val sale: Models.Sale? = null,
        val businessName: String? = null
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class UpdateBillingDataIntent(val updateSaleItemRequest: Models.UpdateSaleItemRequest) : Intent()

        object ShowDeleteDialogIntent : Intent()

        object GoToMerchantProfile : Intent()

        data class DeleteSaleIntent(val saleId: String) : Intent()

        data class ShowLoaderIntent(val canShow: Boolean) : Intent()

        data class ShowErrorIntent(val msg: String) : Intent()

        data class ShareIntent(val sale: Models.Sale?) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetAuthToken(val authToken: String) : PartialState()

        data class SetSaleId(val saleId: String) : PartialState()

        data class SetSale(val sale: Models.Sale) : PartialState()

        data class SetBusinessName(val name: String?) : PartialState()

        data class SetEditable(val isEditable: Boolean) : PartialState()

        data class SetLoading(val canShow: Boolean) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToLoginScreen : ViewEvent()
        object GoToMerchantProfileScreen : ViewEvent()
        data class ShowError(val msg: String) : ViewEvent()
        object ReLoadWebView : ViewEvent()
        data class LoadWebView(val saleId: String, val authToken: String) : ViewEvent()
        data class ShowDeleteDialog(val saleId: String) : ViewEvent()
        object OnDeleted : ViewEvent()
        object ShareAsImage : ViewEvent()
        object ShareAsPDF : ViewEvent()
        data class UpdateSale(val sale: String) : ViewEvent()
    }
}
