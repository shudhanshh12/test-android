package `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount

import `in`.okcredit.backend._offline.usecase.GetTransaction
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerContract
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountContract.*
import `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount.UpdateTransactionAmountContract.PartialState.*
import `in`.okcredit.merchant.customer_ui.usecase.UpdateTransactionAmount
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateTransactionAmountViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(CustomerContract.ARG_TXN_ID) val transactionId: String,
    private val updateTransactionAmount: Lazy<UpdateTransactionAmount>,
    private val getTransaction: Lazy<GetTransaction>,
    private val getCustomer: Lazy<GetCustomer>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private val getCustomerPublishSubject: PublishSubject<String> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(

            intent<Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<PartialState> { HideAlert }
                        .startWith(ShowAlert(it.message))
                },

            intent<Intent.UpdateTransactionAmount>()
                .switchMap {
                    updateTransactionAmount.get()
                        .execute(UpdateTransactionAmount.Request(it.transactionAmount, transactionId))
                }.map {
                    when (it) {
                        is Result.Progress -> ShowLoading
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.AmountUpdatedSuccessfully)
                            HideLoading
                        }
                        is Result.Failure -> ShowAlert("Some failure occurred")
                    }
                },

            intent<Intent.Load>()
                .switchMap {
                    getTransaction.get().execute(transactionId)
                }.map {
                    getCustomerPublishSubject.onNext(it.customerId)
                    SetTransaction(it)
                },

            intent<Intent.Load>()
                .switchMap {
                    getCustomerPublishSubject
                }.switchMap { getCustomer.get().execute(it) }
                .map {
                    SetCustomer(it)
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is ShowLoading -> currentState.copy(isLoading = true)
            is ErrorState -> currentState.copy(
                isLoading = false,
                error = true
            )
            is ShowAlert -> currentState.copy(
                isLoading = false,
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is HideAlert -> currentState.copy(isAlertVisible = false)
            is SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is ClearNetworkError -> currentState.copy(networkError = false)
            is SetTransaction -> currentState.copy(transaction = partialState.transaction)
            is SetCustomer -> currentState.copy(customer = partialState.customer)
            is NoChange -> currentState
            HideLoading -> currentState.copy(isLoading = false)
        }
    }
}
