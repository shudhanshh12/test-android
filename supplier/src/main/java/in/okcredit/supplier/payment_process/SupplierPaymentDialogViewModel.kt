package `in`.okcredit.supplier.payment_process

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import javax.inject.Inject

class SupplierPaymentDialogViewModel @Inject constructor(
    initialState: SupplierPaymentDialogContract.State,
) : BaseViewModel<SupplierPaymentDialogContract.State, SupplierPaymentDialogContract.PartialState, SupplierPaymentDialogContract.ViewEvent>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<SupplierPaymentDialogContract.State>> {
        return Observable.mergeArray(
            onConfirmClicked(),
            onChangeClicked()
        )
    }

    private fun onConfirmClicked(): Observable<SupplierPaymentDialogContract.PartialState> {
        return intent<SupplierPaymentDialogContract.Intent.OnConfirmClicked>()
            .map {
                emitViewEvent(SupplierPaymentDialogContract.ViewEvent.OnConfirmClicked)
                SupplierPaymentDialogContract.PartialState.NoChange
            }
    }

    private fun onChangeClicked(): Observable<SupplierPaymentDialogContract.PartialState> {
        return intent<SupplierPaymentDialogContract.Intent.OnChangeDetails>()
            .map {
                emitViewEvent(SupplierPaymentDialogContract.ViewEvent.OnChangeDetails)
                SupplierPaymentDialogContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: SupplierPaymentDialogContract.State,
        partialState: SupplierPaymentDialogContract.PartialState
    ): SupplierPaymentDialogContract.State {
        return when (partialState) {
            SupplierPaymentDialogContract.PartialState.NoChange -> currentState
        }
    }
}
