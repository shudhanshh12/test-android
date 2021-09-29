package tech.okcredit.bill_management_ui.billdetail

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.billdetail.BillDetailContract.*
import tech.okcredit.bill_management_ui.billdetail.BillDetailContract.PartialState.NoChange
import tech.okcredit.bill_management_ui.billdetail.BillDetailContract.PartialState.SetBill
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.analytics.BillTracker
import tech.okcredit.sdk.usecase.DeleteBill
import tech.okcredit.sdk.usecase.GetBillForId
import tech.okcredit.sdk.usecase.SaveBillsOnDevice
import java.io.File
import javax.inject.Inject

class BillDetailViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(BILL_INTENT_EXTRAS.BILL_ID) val billId: String?,
    @ViewModelParam(BILL_INTENT_EXTRAS.ROLE) val role: String?,
    @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_NAME) val accName: String?,
    private val saveBillsOnDevice: Lazy<SaveBillsOnDevice>,
    private val getBillDetails: Lazy<GetBillForId>,
    private val deleteBill: Lazy<DeleteBill>,
    private val billTracker: Lazy<BillTracker>
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
                            billTracker.get().trackBillViewed(it.value, it.value.localBillDocList.size)
                            SetBill(it.value, list, billId, role, accName)
                        }
                        is Result.Progress -> NoChange
                    }
                },
            intent<Intent.Delete>().switchMap {
                deleteBill.get().execute(DeleteBill.Request(billId!!))
            }.map {
                when (it) {
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.GoToBillScreen)
                        NoChange
                    }
                    else -> NoChange
                }
            },
            intent<Intent.DownloadBill>().switchMap {
                UseCase.wrapCompletable(saveBillsOnDevice.get().execute(billId!!))
            }.map {
                when (it) {
                    is Result.Failure -> NoChange
                    is Result.Success -> {
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
                billId = partialState.billID,
                role = partialState.role,
                accName = partialState.accName
            )
        }
    }
}
