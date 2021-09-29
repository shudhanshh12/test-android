package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class AddTxnContainerViewModel @Inject constructor(
    initialState: State,
    private val getCustomer: Lazy<GetCustomer>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadCustomerDetails(),
        )
    }

    private fun loadCustomerDetails() = intent<Intent.Load>().switchMap {
        wrap(getCustomer.get().execute(getCurrentState().customerId))
    }.map {
        when (it) {
            is Result.Success -> {
                PartialState.CustomerData(it.value)
            }
            is Result.Failure -> {
                when {
                    isInternetIssue(it.error) -> {
                        PartialState.SetLoading(false)
                    }
                    else -> {
                        emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                        PartialState.SetLoading(false)
                    }
                }
            }
            is Result.Progress -> PartialState.SetLoading(true)
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.CustomerData -> currentState.copy(
                customerName = partialState.customer.description,
                customerProfile = partialState.customer.profileImage,
                balanceDue = partialState.customer.balanceV2,
                loading = false
            )
            PartialState.NoChange -> currentState
            is PartialState.SetLoading -> currentState.copy(loading = partialState.loading)
        }
    }
}
