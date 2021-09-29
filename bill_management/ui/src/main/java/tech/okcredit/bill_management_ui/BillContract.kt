package tech.okcredit.bill_management_ui

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.sdk.models.SelectedDate
import tech.okcredit.sdk.models.SelectedDateMode
import tech.okcredit.sdk.store.database.LocalBill

interface BillContract {

    sealed class Filter {
        object All : Filter()
    }

    data class State(
        val areBillsPresent: Boolean = false,
        val accountMobile: String? = null,
        val lastSeenTime: String = DateTimeUtils.currentDateTime().millis.toString(),
        val accountName: String? = null,
        val isLoading: Boolean = true,
        val activeFilter: Filter = Filter.All,
        val map: MutableMap<String, MutableList<LocalBill>>? = null,
        val accountId: String? = null,
        val mobile: String? = null,
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
        val selectedMode: SelectedDateMode = SelectedDateMode.CURRENT,
        val isCollectionAdopted: Boolean = false,
        val isDateFilterEducationShown: Boolean? = null,
        val isReportShareEducationShow: Boolean = false,
        val role: String? = null,
        val monthsList: List<String>? = null,
        val current: DateTime? = null,
        val last: DateTime? = null,
        val lastToLast: DateTime? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class AreBillsPresent(val areBillsPresent: Boolean) : PartialState()

        data class UpdateTimeSet(val lastSeenTime: String) : PartialState()

        data class SetAccountDetails(val accountName: String?, val accountID: String?) : PartialState()

        data class SetBills(
            val map: MutableMap<String, MutableList<LocalBill>>,
            val role: String,
            val selectedMode: SelectedDateMode
        ) : PartialState()

        data class SetBillsAndMonths(
            val map: MutableMap<String, MutableList<LocalBill>>,
            val role: String,
            val monthsList: List<String>,
            val current: DateTime? = null,
            val last: DateTime? = null,
            val lastToLast: DateTime? = null,
            val selectedMode: SelectedDateMode
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class OnDateChange(val selectedDate: SelectedDate) : Intent()

        data class PageViewed(val screen: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object ShowBottomSheetTutorial : ViewEvent()
    }
}
