package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.collection.contract.CollectionSyncer.Source.ONLINE_PAYMENTS
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract.*
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract.PartialState.*
import `in`.okcredit.collection_ui.ui.passbook.payments.views.OnlinePaymentsView
import `in`.okcredit.collection_ui.usecase.GetNewOnlinePayments
import `in`.okcredit.collection_ui.usecase.GetOnlinePayments
import `in`.okcredit.collection_ui.usecase.GetTransactionIdFromCollection
import `in`.okcredit.collection_ui.usecase.ScheduleSyncCollections
import `in`.okcredit.merchant.collection.usecase.SetPaymentTagViewed
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class OnlinePaymentsViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val getOnlinePayments: Lazy<GetOnlinePayments>,
    private val getTransactionIdFromCollection: Lazy<GetTransactionIdFromCollection>,
    private val setPaymentTagViewed: Lazy<SetPaymentTagViewed>,
    private val getNewOnlinePayments: Lazy<GetNewOnlinePayments>,
    private val scheduleSyncCollections: Lazy<ScheduleSyncCollections>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            getOnlinePayments(),
            setFilterRange(),
            observeItemClicked(),
            getNewOnlinePaymentsCount(),
            setPaymentTagViewed(),
            setTransactionFilter(),
            scheduleSyncCollections(),
        )
    }

    private fun getOnlinePayments() = intent<Intent.GetOnlinePayments>()
        .switchMap {
            getOnlinePayments.get().execute(GetOnlinePayments.Request(it.startTime, it.endTime))
        }
        .map {
            when (it) {
                is Result.Progress -> NoChange
                is Result.Success -> {
                    emitViewEvent(ViewEvent.OnLoadDataSuccessEvent(it.value.isNotEmpty()))
                    SetOnlinePaymentList(it.value)
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.OnError)
                    NoChange
                }
            }
        }

    private fun setFilterRange() = intent<Intent.ChangeFilterRange>()
        .map {
            SetFilterRange(it.filterRange)
        }

    private fun setTransactionFilter() = intent<Intent.ChangeTransactionFilter>()
        .map {
            SetTransactionFilter(it.filter)
        }

    private fun observeItemClicked() = intent<Intent.ItemClicked>().switchMap {
        return@switchMap wrap(
            if (it.collectionOnlinePayment.accountId.isNotEmpty() &&
                (
                    it.collectionOnlinePayment.status == PaymentStatus.COMPLETE.value ||
                        it.collectionOnlinePayment.status == PaymentStatus.FAILED.value ||
                        it.collectionOnlinePayment.status == PaymentStatus.REFUNDED.value
                    )
            ) {
                getTransactionIdFromCollection.get().execute(it.collectionOnlinePayment.id)
            } else {
                Single.just(it.collectionOnlinePayment.id to "")
            }
        )
    }.map {
        if (it is Result.Success) {
            if (it.value.second.isNotEmpty()) {
                emitViewEvent(ViewEvent.MoveToTransactionDetails(it.value.second))
            } else {
                emitViewEvent(ViewEvent.MoveToPaymentDetails(it.value.first))
            }
        }
        NoChange
    }

    private fun setPaymentTagViewed() = intent<Intent.SetPaymentTag>()
        .switchMap {
            wrap(setPaymentTagViewed.get().execute())
        }
        .map {
            NoChange
        }

    private fun getNewOnlinePaymentsCount() = intent<Intent.Load>()
        .switchMap { getNewOnlinePayments.get().execute(Unit) }
        .map {
            if (it is Result.Success) {
                if (it.value.isNotEmpty())
                    pushIntent(Intent.SetPaymentTag)
            }
            NoChange
        }

    private fun scheduleSyncCollections() = intent<Intent.Load>()
        .switchMap { wrap(scheduleSyncCollections.get().execute(ONLINE_PAYMENTS)) }
        .map {
            NoChange
        }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is SetOnlinePaymentList -> setOnlinePaymentList(currentState, partialState)
            is SetFilterRange -> currentState.copy(filter = partialState.filterRange)
            is SetTransactionFilter -> filterTransactionData(currentState, partialState)
        }
    }

    private fun filterTransactionData(
        currentState: State,
        partialState: SetTransactionFilter,
    ): State {
        val filteredList = currentState.originalList.filter {
            return@filter when (partialState.filter) {
                TransactionFilter.CUSTOMER_COLLECTIONS ->
                    it.collectionOnlinePayment.type != OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION
                TransactionFilter.SUPPLIER_COLLECTIONS ->
                    it.collectionOnlinePayment.type == OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION
                else -> true
            }
        }

        return currentState.copy(
            transactionFilter = partialState.filter,
            filteredList = filteredList,
        )
    }

    private fun setOnlinePaymentList(
        currentState: State,
        partialState: SetOnlinePaymentList,
    ): State {
        var customerPaymentTotal = 0.0
        var supplierPaymentTotal = 0.0
        val filteredList = mutableListOf<GetOnlinePayments.OnlineCollectionData>()
        partialState.onlinePayments.forEach {
            if (it.collectionOnlinePayment.status == CollectionStatus.COMPLETE) {
                if (it.collectionOnlinePayment.type == OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION) {
                    supplierPaymentTotal += it.collectionOnlinePayment.amount
                } else {
                    customerPaymentTotal += it.collectionOnlinePayment.amount
                }
            }

            when (currentState.transactionFilter) {
                TransactionFilter.CUSTOMER_COLLECTIONS -> {
                    if (it.collectionOnlinePayment.type != OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION) {
                        filteredList.add(it)
                    }
                }
                TransactionFilter.SUPPLIER_COLLECTIONS -> {
                    if (it.collectionOnlinePayment.type == OnlinePaymentsView.TYPE_SUPPLIER_COLLECTION) {
                        filteredList.add(it)
                    }
                }
                TransactionFilter.ALL -> {
                    filteredList.add(it)
                }
            }
        }

        return currentState.copy(
            filteredList = filteredList,
            totalCustomerPayments = customerPaymentTotal.toLong(),
            totalSupplierPayments = supplierPaymentTotal.toLong(),
        )
    }
}
