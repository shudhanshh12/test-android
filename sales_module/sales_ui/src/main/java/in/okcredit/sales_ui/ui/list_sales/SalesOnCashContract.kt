package `in`.okcredit.sales_ui.ui.list_sales

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime

interface SalesOnCashContract {
    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val list: List<Models.Sale>? = null,
        val totalAmount: Double = 0.0,
        val filter: SalesOnCashFragment.Filter? = null,
        val startDate: DateTime? = null,
        val endDate: DateTime? = null,
        val videoUrl: String = "dIZ2khCCZac",
        val videoUrlWithBill: String = "40bq0ExzPf0",
        val canYoutubeVideoShow: Boolean = false,
        val isFirstTime: Boolean = false,
        val canShowAlert: Boolean = false,
        val isBillingAbEnabled: Boolean = false,
        val isBillingInfoGraphicAbEnabled: Boolean = false,
        val alert: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetAllCashSales(val sales: Models.SalesListResponse) : PartialState()

        data class SetFirstTime(val firstTime: Boolean) : PartialState()

        data class SetSales(val sales: Models.SalesListResponse) : PartialState()

        data class ChangeFilter(val filter: SalesOnCashFragment.Filter) : PartialState()

        data class ShowYoutubeVideo(val canShow: Boolean) : PartialState()

        data class setDate(val startDate: DateTime, val endDate: DateTime) : PartialState()

        data class ShowAlert(val msg: String) : PartialState()

        object HideAlert : PartialState()

        data class SetBillingAb(val isBillingAbEnabled: Boolean) : PartialState()

        data class SetBillingInfoGraphicAb(val isBillingInfoGraphicAbEnabled: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object GetAllSales : Intent()

        data class GetSales(val startDate: DateTime, val endDate: DateTime) : Intent()

        data class DeleteSale(val id: String) : Intent()

        data class ChangeFilter(val filter: SalesOnCashFragment.Filter) : Intent()

        data class SetDate(val startDate: DateTime, val endDate: DateTime) : Intent()

        object OnAddSaleIntent : Intent()

        object OnNewBillIntent : Intent()

        data class ShowSaleDetailIntent(val saleid: String) : Intent()

        data class ShowBillSummaryIntent(val saleid: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent

    interface Navigator {

        fun showAll()
        fun showToday()
        fun showThisMonth()
        fun showLastMonth()
        fun reLoad()
        fun onDeleted(saleid: String)

        fun gotoLogin()
        fun gotoSalesDetailScreen(saleId: String)
        fun gotoBillSummaryScreen(saleId: String)
        fun gotoAddSaleScreen()
        fun gotoBillItemScreen()
    }
}
