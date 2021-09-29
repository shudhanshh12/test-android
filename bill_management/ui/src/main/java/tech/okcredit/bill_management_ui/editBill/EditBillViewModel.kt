package tech.okcredit.bill_management_ui.editBill

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.editBill.EditBillContract.*
import tech.okcredit.bill_management_ui.editBill.EditBillContract.PartialState.NoChange
import tech.okcredit.bill_management_ui.editBill.EditBillContract.PartialState.SetBill
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.usecase.AddNewBillDocs
import tech.okcredit.sdk.usecase.DeleteBill
import tech.okcredit.sdk.usecase.DeleteBillDoc
import tech.okcredit.sdk.usecase.GetBillForId
import java.io.File
import javax.inject.Inject

class EditBillViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val getBillDetails: Lazy<GetBillForId>,
    private val addNewBillDocs: Lazy<AddNewBillDocs>,
    private val deletedBillDoc: Lazy<DeleteBillDoc>,
    private val deleteBill: Lazy<DeleteBill>,
    @ViewModelParam(BILL_INTENT_EXTRAS.BILL_ID) val billId: String?,
    @ViewModelParam(BILL_INTENT_EXTRAS.BILL_POSITION) val initialPosition: Int?
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .switchMap { wrap(getBillDetails.get().execute(billId!!)) }
                .map {
                    when (it) {
                        is Result.Failure -> NoChange
                        is Result.Success -> {
                            val list = ArrayList<CapturedImage>()
                            it.value.localBillDocList.forEach {
                                val file = File(it.url)
                                list.add(CapturedImage(file))
                            }
                            SetBill(it.value, list, initialPosition)
                        }
                        is Result.Progress -> NoChange
                    }
                },
            intent<Intent.NewImages>()
                .switchMap { addNewBillDocs.get().execute(AddNewBillDocs.Request(billId!!, it.list)) }
                .map {
                    when (it) {
                        is Result.Failure -> NoChange
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.EditBillEvent(it.value.addedImageCount))
                            NoChange
                        }
                        is Result.Progress -> NoChange
                    }
                },

            intent<Intent.DeleteBillDoc>()
                .switchMap {
                    deletedBillDoc.get().execute(DeleteBillDoc.Request(billDocId = it.billDocId, billId = billId!!))
                }
                .map {
                    when (it) {
                        is Result.Failure -> NoChange
                        is Result.Success -> {
                            NoChange
                        }
                        is Result.Progress -> NoChange
                    }
                },

            intent<Intent.DeleteBill>()
                .switchMap { deleteBill.get().execute(DeleteBill.Request(billId!!)) }
                .map {
                    when (it) {
                        is Result.Failure -> NoChange
                        is Result.Success -> {
                            ViewEvent.GoBack
                            NoChange
                        }
                        is Result.Progress -> NoChange
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
            is SetBill -> currentState.copy(
                localBill = partialState.localBill,
                imageList = partialState.list,
                initialPosition = partialState.intialPosition
            )
        }
    }
}
