package `in`.okcredit.frontend.ui.account_statement

import `in`.okcredit.backend._offline.model.TransactionWrapper
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import androidx.lifecycle.LifecycleOwner
import org.joda.time.DateTime
import java.lang.ref.WeakReference

interface AccountStatementContract {

    data class State(
        val isLoading: Boolean = true,
        val isLoadingDownload: Boolean = false,
        val transactions: List<TransactionWrapper> = arrayListOf(),
        val totalPaymentAmount: Long = 0,
        val totalPaymentCount: Int = 0,
        val totalCreditCount: Int = 0,
        var totalCreditAmount: Long = 0,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val isShowOld: Boolean = false,
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isShowDownloadAlert: Boolean = false,
        val startDate: DateTime = CommonUtils.currentDateTime().withTimeAtStartOfDay(),
        val endDate: DateTime = CommonUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay().minusMillis(1),
        val isOnlineTransactionSelected: Boolean = false,
        val sourceScreen: String = "",
        val totalDiscountAmount: Long = 0,
        val totalDiscountCount: Int = 0,
        val downloadedFileUriString: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        data class ShowData(
            val transaction: List<TransactionWrapper>,
            var totalCreditAmount: Long,
            val totalCreditCount: Int,
            val totalPaymentAmount: Long,
            val totalPaymentCount: Int,
            val isShowOld: Boolean,
            val totalDiscountAmount: Long,
            val totalDiscountCount: Int
        ) : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class ChangeDateRange(val startDate: DateTime, val endDate: DateTime) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class ShowDownloadAlert(val downloadedFilePath: String?) : PartialState()

        object HideDownloadAlert : PartialState()

        data class ChangeShowOldStatus(val status: Boolean) : PartialState()

        data class SetDownloadLoadingStatus(val status: Boolean) : PartialState()

        data class SetOnlineTxnSelected(val isOnlineTransactionSelected: Boolean) : PartialState()

        data class SetSourceScreen(val sourceScreen: String, val isOnlineTransactionSelected: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // show alert
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object LoadOldTxns : Intent()

        // selected different Date range
        data class ChangeDateRange(val startDate: DateTime, val endDate: DateTime) : Intent()

        data class DownloadStatement(val startDate: DateTime, val endDate: DateTime) : Intent()

        object HideDownloadAlert : Intent()

        data class SelectOnlineTransactions(val isOnlineTransationSelected: Boolean) : Intent()

        data class ObserveWorkerStatus(val weakLifecycleOwner: WeakReference<LifecycleOwner>) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
