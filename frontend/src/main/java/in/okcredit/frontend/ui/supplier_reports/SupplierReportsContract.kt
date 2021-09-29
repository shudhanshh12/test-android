package `in`.okcredit.frontend.ui.supplier_reports

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import androidx.lifecycle.LifecycleOwner
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope
import java.lang.ref.WeakReference

interface SupplierReportsContract {

    data class SelectedDate(
        val startDate: DateTime,
        val endDate: DateTime,
        val selectedMode: SelectedDateMode,
    )

    enum class SelectedDateMode {
        CUSTOM_DATE,
        THIS_MONTH,
        LAST_WEEK,
        LAST_ZERO_BALANCE,
        LAST_MONTH,
        LAST_THREE_MONTHS,
        LAST_SIX_MONTHS,
        OVERALL
    }

    data class SupplierStatementResponse(
        val transactions: List<Transaction>,
        val paymentTransactionCount: Int,
        val creditTransactionCount: Int,
        val balanceForSelectedDateRange: Long,
        val totalPayment: Long,
        val totalCredit: Long,
    )

    data class State(
        val showDownloadLoader: Boolean = false,
        val isAlertVisible: Boolean = false,
        val messageId: Int? = null,
        val error: Boolean = false,
        val networkError: Boolean = false,
        val startDate: DateTime = CommonUtils.currentDateTime().withTimeAtStartOfDay(),
        val endDate: DateTime = CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1),
        val transactions: List<Transaction>? = null,
        val supplier: Supplier? = null,
        val supplierId: String = "",
        val paymentTransactionCount: Int = 0,
        val creditTransactionCount: Int = 0,
        val balanceForSelectedDuration: Long = 0,
        val totalPayment: Long = 0,
        val totalCredit: Long = 0,
        val selectedMode: SelectedDateMode = SelectedDateMode.THIS_MONTH,
        val isCollectionAdopted: Boolean = false,
        val isDateFilterEducationShown: Boolean? = null,
        val isReportShareEducationShown: Boolean? = null,
        val isReportDownloading: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class ShowDownloadLoader(val showDownloadLoader: Boolean) : PartialState()

        data class ShowError(val error: Boolean) : PartialState()

        data class ShowAlert(val messageId: Int) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class ShowData(
            val transactions: List<Transaction>,
            val paymentTransactionCount: Int,
            val creditTransactionCount: Int,
            val balanceForSelectedDuration: Long,
            val totalPayment: Long,
            val totalCredit: Long,
        ) : PartialState()

        data class ShowAllTransactionsData(
            val transactions: List<Transaction>,
            val paymentTransactionCount: Int,
            val creditTransactionCount: Int,
            val balanceForSelectedDuration: Long,
            val totalPayment: Long,
            val totalCredit: Long,
            val selectedMode: SelectedDateMode,
            val startDate: DateTime,
            val endDate: DateTime,
        ) : PartialState()

        data class ChangeDateRange(
            val supplierId: String,
            val startDate: DateTime,
            val endDate: DateTime,
            val selectedMode: SelectedDateMode,
        ) : PartialState()

        data class SetCollectionActivated(val isCollectionAdopted: Boolean) : PartialState()

        data class IsDateFilterEducationShown(val isDateFilterEducationShown: Boolean) : PartialState()

        data class SetCustomer(val customer: Supplier) : PartialState()

        data class IsReportShareEducationShown(val isReportShareEducationShown: Boolean) : PartialState()

        data class SetReportDownloading(val isDownloading: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class ChangeDateRange(val startDate: DateTime, val endDate: DateTime, val selectedMode: SelectedDateMode) :
            Intent()

        object GetMiniStatementDateRange : Intent()

        data class DownloadReport(val type: String, val startDate: DateTime, val endDate: DateTime) : Intent()

        object ShareReportClicked : Intent()

        data class GetAllTransactions(val selectedMode: SelectedDateMode) : Intent()

        data class RxPreferenceBoolean(val key: String, val value: Boolean, val scope: Scope) : Intent()

        object UpdateMobileAndShareReport : Intent()

        data class ObserveDownloadReportWorkerStatus(val weakLifecycleOwner: WeakReference<LifecycleOwner>) : Intent()

        data class ShareReportToWhatsapp(val url: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToLogin : ViewEvent()

        object GoBack : ViewEvent()

        data class ShareReport(val intent: android.content.Intent) : ViewEvent()

        data class TrackPreviewEvent(val noResult: Boolean) : ViewEvent()

        object StartShareReport : ViewEvent()

        data class ReportGenerationFailed(val isInternetIssue: Boolean = false) : ViewEvent()

        object DownloadedAlert : ViewEvent()
    }
}
