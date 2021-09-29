package tech.okcredit.bill_management_ui.selected_bills.selectedimage

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import org.joda.time.DateTime
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.models.RawBill

interface SelectedImageContract {

    data class State(
        val date: DateTime = CommonUtils.currentDateTime(),
        val isLoading: Boolean = true,
        val imageList: ArrayList<CapturedImage> = ArrayList(),
        val note: String? = null,
        val stateChanged: Boolean = false,
        val submitLoading: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class LoadImages(val images: java.util.ArrayList<CapturedImage>) : PartialState()

        object ShowLoader : PartialState()
        object HideLoader : PartialState()
        object NoChange : PartialState()
        data class OnStateChange(val stateChanged: Boolean) : PartialState()
        data class ChangeDate(val value: DateTime) : PartialState()
        data class UpdateNote(val note: String?) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class OnChangeDate(val date: DateTime) : Intent()
        data class OnDoneClicked(val bill: RawBill) : Intent()
        data class UpdateNote(val note: String?) : Intent()
        data class StateChange(val stateChanged: Boolean) : Intent()
        data class DeleteImage(val pair: Pair<CapturedImage, ArrayList<CapturedImage>>) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GoBack(val billId: String) : ViewEvent()
    }
}
