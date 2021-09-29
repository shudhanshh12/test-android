package merchant.okcredit.user_stories.storypreview

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.camera_contract.CapturedImage

interface StoryPreviewContract {
    data class State(
        val imageList: ArrayList<CapturedImage> = ArrayList(),
        val imageCaptionMap: HashMap<CapturedImage?, String>? = HashMap(),
        val selectedImage: CapturedImage? = null,
        val caption: String? = null,
        val isLoading: Boolean = false,
        val activeMerchantId: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class LoadImages(
            val images: java.util.ArrayList<CapturedImage>,
            val captureMap: HashMap<CapturedImage?, String>?,
            val isLoading: Boolean,
            val image: CapturedImage,
            val activeMerchantId: String
        ) : PartialState()

        data class SetImages(
            val images: java.util.ArrayList<CapturedImage>,
            val captureImage: CapturedImage
        ) : PartialState()

        data class CurrentImage(val image: CapturedImage) : PartialState()
        data class AddCaption(val caption: String) : PartialState()
        object NoChange : PartialState()
        object Loading : PartialState()
        object Error : PartialState()
        object Saved : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class DeleteImage(val pair: Pair<CapturedImage, ArrayList<CapturedImage>>) : Intent()
        data class CurrentSelectedImage(val image: CapturedImage) : Intent()
        data class AddCaption(val caption: String) : Intent()
        data class SaveStory(
            val captureImage: List<CapturedImage>,
            val captionMap: HashMap<CapturedImage?, String>?
        ) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToMyStoryScreen : ViewEvent()
    }
}
