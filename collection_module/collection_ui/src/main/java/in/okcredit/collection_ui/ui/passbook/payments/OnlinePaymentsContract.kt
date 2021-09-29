package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection_ui.usecase.GetOnlinePayments
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime

interface OnlinePaymentsContract {

    data class State(
        val source: String = "",
        val isLoading: Boolean = true,
        val totalCustomerPayments: Long = 0L,
        val totalSupplierPayments: Long = 0L,
        val transactionFilter: TransactionFilter = TransactionFilter.ALL,
        val filteredList: List<GetOnlinePayments.OnlineCollectionData> = listOf(),
        val originalList: List<GetOnlinePayments.OnlineCollectionData> = listOf(),
        val filter: DateFilterListDialog.Filters = DateFilterListDialog.Filters.Today
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetOnlinePaymentList(val onlinePayments: List<GetOnlinePayments.OnlineCollectionData>) :
            PartialState()

        data class SetFilterRange(val filterRange: DateFilterListDialog.Filters) : PartialState()

        data class SetTransactionFilter(val filter: TransactionFilter) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class GetOnlinePayments(val startTime: DateTime, val endTime: DateTime) : Intent()

        data class ChangeFilterRange(val filterRange: DateFilterListDialog.Filters) : Intent()

        data class ItemClicked(val collectionOnlinePayment: CollectionOnlinePayment) : Intent()

        data class ChangeTransactionFilter(val filter: TransactionFilter) : Intent()

        object SetPaymentTag : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class OnChangeFilter(val filter: DateFilterListDialog.Filters) : ViewEvent()
        data class OnLoadDataSuccessEvent(val isNotEmpty: Boolean) : ViewEvent()
        data class MoveToTransactionDetails(val transactionId: String) : ViewEvent()
        data class MoveToPaymentDetails(val paymentId: String) : ViewEvent()
        object OnError : ViewEvent()
    }

    enum class PaymentStatus(val value: Int) {
        UNKOWN(0), ACTIVE(1), PAID(2), EXPIRED(3), CANCELLED(4), COMPLETE(5), FAILED(6), REFUNDED(7),
        REFUND_INITIATED(8),
        PAYOUT_FAILED(9),
        MIGRATED(10),
        PAYOUT_INITIATED(11),
    }
}
