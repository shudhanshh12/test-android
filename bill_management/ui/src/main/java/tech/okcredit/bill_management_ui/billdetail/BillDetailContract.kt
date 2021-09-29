package tech.okcredit.bill_management_ui.billdetail

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.store.database.LocalBill

interface BillDetailContract {

    data class State(
        val isLoading: Boolean = true,
        val localBill: LocalBill? = null,
        val billId: String? = null,
        val role: String? = null,
        val imageList: ArrayList<CapturedImage>? = null,
        val accName: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetBill(
            val localBill: LocalBill,
            val list: ArrayList<CapturedImage>,
            val billID: String?,
            val role: String?,
            val accName: String?
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        data class Delete(val billId: String) : Intent()

        data class DownloadBill(val billId: String) : Intent()

        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToBillScreen : ViewEvent()
    }
}
