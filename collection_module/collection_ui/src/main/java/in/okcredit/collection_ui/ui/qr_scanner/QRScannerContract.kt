package `in`.okcredit.collection_ui.ui.qr_scanner

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface QRScannerContract {

    data class State(
        val error: Boolean = false,
        val isTorchOn: Boolean = false,
        val isStoragePermissionGranted: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SetTorchOn(val isTorchOn: Boolean) : PartialState()

        data class SetStoragePermissionStatus(val isStoragePermissionGranted: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class SetTorchOn(val isTorchOn: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
