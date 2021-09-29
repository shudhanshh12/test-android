package tech.okcredit.home.ui.acccountV2.ui

import `in`.okcredit.backend._offline.usecase.GetAccountSummary
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport.ReportType
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.supplier.statement.usecase.GetSupplierBalanceAndCount
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

interface AccountContract {

    data class State(
        val source: String = "",
        val accountSummary: GetAccountSummary.AccountSummary? = null,
        val merchantName: String = "",
        val isLoading: Boolean = false,
        val isInternetConnected: Boolean = false,
        val supplierSummary: GetSupplierBalanceAndCount.Response? = null,
        val isWebTestingActivated: Boolean = false,
        val downloadedFileUriString: String? = null,
        val isDownloaded: Boolean = false,
        val shouldShowLastUpdatedAtText: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : AccountContract.PartialState()
        data class SetAccountSummary(val accountSummary: GetAccountSummary.AccountSummary) :
            AccountContract.PartialState()

        data class SetMerchantName(val merchantName: String) : AccountContract.PartialState()

        data class SetLoading(val isLoading: Boolean) : AccountContract.PartialState()

        data class SetSupplierBalanceAndCount(val supplierSummary: GetSupplierBalanceAndCount.Response) : PartialState()

        data class SetWebTesting(val isWebTestingActivated: Boolean) : AccountContract.PartialState()

        data class SetDownloadStatus(val downloadedFilePath: String?) : PartialState()

        data class SetShouldShowLastUpdatedAtText(val shouldShowLastUpdatedAtText: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : AccountContract.Intent()

        data class CheckSupplierCreditFeatureAndObserveWorkerStatus(
            val weakLifecycleOwner: WeakReference<LifecycleOwner>,
        ) : AccountContract.Intent()

        object CustomerKhataClick : AccountContract.Intent()

        object SupplierKhataClick : AccountContract.Intent()

        object DownloadReport : AccountContract.Intent()

        object OnReportUrlGenerated : AccountContract.Intent()

        object WebLibraryClick : AccountContract.Intent()

        data class ObserveDownloadReportWorkerStatus(
            val weakLifecycleOwner: WeakReference<LifecycleOwner>,
            val reportType: ReportType,
        ) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToAccountStatementScreen : AccountContract.ViewEvent()

        object GoToSupplierStatementScreen : AccountContract.ViewEvent()

        object StartDownload : AccountContract.ViewEvent()

        object DeepLinkStartDownload : AccountContract.ViewEvent()

        data class ReportGenerationFailed(val isInternetIssue: Boolean = false) : AccountContract.ViewEvent()

        object GoToWebWebViewScreen : AccountContract.ViewEvent()
    }
}
