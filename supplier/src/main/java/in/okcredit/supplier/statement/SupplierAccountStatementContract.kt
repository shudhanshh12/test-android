package `in`.okcredit.supplier.statement

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import androidx.lifecycle.LifecycleOwner
import org.joda.time.DateTime
import java.lang.ref.WeakReference

object SupplierAccountStatementContract {

    data class State(
        val showLoadMore: Boolean = false,
        val error: Boolean = false,
        val startDate: DateTime = CommonUtils.currentDateTime().withTimeAtStartOfDay(),
        val endDate: DateTime = CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1),
        val sourceScreen: String = "",
        val showOnlyOnlineTransactions: Boolean = false,
        val summaryModel: AccountStatementModel.StatementSummary? = null,
        val transactionModels: List<AccountStatementModel.Transaction>? = emptyList(),
        val statementModels: List<AccountStatementModel> = emptyList(),
        val showDownloadAlert: Boolean = false,
        val isDownloading: Boolean = false,
        val downloadedFileUriString: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ErrorState : PartialState()

        data class ChangeDateRange(val startDate: DateTime, val endDate: DateTime) : PartialState()

        object NoChange : PartialState()

        object ShowLoading : PartialState()

        object NetworkError : PartialState()

        data class ShowOnlyOnlineTransactions(val showOnlyOnlineTransactions: Boolean) : PartialState()

        data class AccountStatementModels(
            val summaryModel: AccountStatementModel.StatementSummary?,
            val transactionModels: List<AccountStatementModel.Transaction>?,
            val showLoadMore: Boolean
        ) : PartialState()

        data class ShowDownloadAlert(val downloadedFilePath: String?) : PartialState()

        data class SetDownloadingStatus(val status: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // show alert
        object Load : Intent()

        object LoadOldTransactions : Intent()

        // selected different Date range
        data class ChangeDateRange(val startDate: DateTime, val endDate: DateTime) : Intent()

        data class DownloadStatement(val startDate: DateTime, val endDate: DateTime) : Intent()

        data class ObserveWorkerStatus(val weakLifecycleOwner: WeakReference<LifecycleOwner>) : Intent()

        data class SelectOnlineTransactions(val isOnlineTransationSelected: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
