package tech.okcredit.bill_management_ui.editBill

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.store.database.LocalBill

interface EditBillContract {

    data class State(
        val isLoading: Boolean = true,
        val localBill: LocalBill? = null,
        val imageList: ArrayList<CapturedImage>? = null,
        val initialPosition: Int? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetBill(val localBill: LocalBill, val list: ArrayList<CapturedImage>, val intialPosition: Int?) :
            PartialState()
    }

    sealed class Intent : UserIntent {
        data class NewImages(val list: List<CapturedImage>) : Intent()
        data class DeleteBillDoc(val billDocId: String) : Intent()
        object Load : Intent()
        object DeleteBill : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoBack : ViewEvent()
        data class EditBillEvent(val count: Int) : ViewEvent()
    }
}
