package tech.okcredit.bill_management_ui.selected_bills.selectedimage

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageContract.*
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageContract.PartialState.*
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.usecase.SubmitBills
import javax.inject.Inject

class SelectedImageViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam("images") val images: ArrayList<CapturedImage>,
    @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_ID) val accountId: String?,
    private val submitBills: Lazy<SubmitBills>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map { LoadImages(images) },
            intent<Intent.Load>()
                .map { ChangeDate(DateTime.now()) },
            intent<Intent.StateChange>()
                .map { OnStateChange(it.stateChanged) },
            // change bill_date
            intent<Intent.OnChangeDate>()
                .map {
                    ChangeDate(it.date)
                },
            intent<Intent.DeleteImage>()
                .map { pair ->
                    pair.pair.second.remove(pair.pair.first)
                    LoadImages(ArrayList(pair.pair.second))
                },
            intent<Intent.UpdateNote>()
                .map {
                    UpdateNote(it.note)
                },
            intent<Intent.OnDoneClicked>().switchMap {
                wrap(submitBills.get().execute(SubmitBills.Request(accountId!!, it.bill)))
            }.map {
                when (it) {
                    is Result.Progress -> ShowLoader
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.GoBack(it.value.billId))
                        HideLoader
                    }
                    is Result.Failure -> HideLoader
                }
            }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is LoadImages -> currentState.copy(imageList = partialState.images)
            is ChangeDate -> currentState.copy(date = partialState.value)
            is OnStateChange -> currentState.copy(stateChanged = partialState.stateChanged)
            is ShowLoader -> currentState.copy(submitLoading = true)
            is HideLoader -> currentState.copy(submitLoading = false)
            is UpdateNote -> currentState.copy(note = partialState.note)
        }
    }
}
