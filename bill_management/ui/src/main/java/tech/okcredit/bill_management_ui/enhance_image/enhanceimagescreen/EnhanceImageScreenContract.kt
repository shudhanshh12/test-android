package tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.camera_contract.CapturedImage
import java.io.File

interface EnhanceImageScreenContract {

    data class State(
        val isLoading: Boolean = true,
        val imageURL: CapturedImage? = null,
        val canEnhanceIamge: Boolean = true,
        val imageList: ArrayList<CapturedImage>? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetImageURL(
            val imageURL: CapturedImage,
            val canEnhanceImage: Boolean
        ) : PartialState()

        data class UpdateImageProp(val list: ArrayList<CapturedImage>, val canEnhanceImage: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        data class UpdateImage(val it: File) : Intent()
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
