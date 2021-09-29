package merchant.okcredit.user_stories.storypreview

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import com.camera.models.models.Picture
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.user_stories.storypreview.StoryPreviewContract.*
import merchant.okcredit.user_stories.usecase.AddUserStory
import merchant.okcredit.user_stories.usecase.GetCapturedImages
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Inject

class StoryPreviewViewModel @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam("images") val images: ArrayList<Picture>,
    @ViewModelParam("captionMap") val captionMap: HashMap<CapturedImage?, String>?,
    @ViewModelParam("activeMerchantId") val activeMerchantId: String,
    private val getCaptureImage: Lazy<GetCapturedImages>,
    private val addUserStory: Lazy<AddUserStory>,
) :
    BaseViewModel<State, PartialState, ViewEvent>(
        initialState = initialState.get()
    ) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .switchMap { wrap(getCaptureImage.get().execute(images)) }
                .map {
                    when (it) {
                        is Result.Success -> {
                            if (!it.value.isNullOrEmpty()) {
                                PartialState.LoadImages(
                                    it.value,
                                    captionMap,
                                    false,
                                    it.value[0],
                                    activeMerchantId
                                )
                            } else {
                                PartialState.Error
                            }
                        }

                        is Result.Progress -> {
                            PartialState.Loading
                        }
                        else -> PartialState.Error
                    }
                },
            intent<Intent.DeleteImage>()
                .map { pair ->
                    pair.pair.second.remove(pair.pair.first)
                    PartialState.SetImages(ArrayList(pair.pair.second), pair.pair.first)
                },
            intent<Intent.CurrentSelectedImage>()
                .map { currentImage ->
                    PartialState.CurrentImage(currentImage.image)
                },
            intent<Intent.AddCaption>()
                .map {
                    PartialState.AddCaption(it.caption)
                },
            intent<Intent.SaveStory>()
                .switchMap { addUserStory.get().execute(AddUserStory.Request(it.captureImage, it.captionMap)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.Loading
                        is Result.Success -> {
                            emitViewEvent(ViewEvent.GoToMyStoryScreen)
                            PartialState.Saved
                        }
                        is Result.Failure -> PartialState.Error
                    }
                }

        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.LoadImages -> currentState.copy(
                imageList = partialState.images,
                imageCaptionMap = partialState.captureMap,
                isLoading = partialState.isLoading,
                selectedImage = partialState.image,
                caption = partialState.captureMap?.get(partialState.image),
                activeMerchantId = partialState.activeMerchantId
            )
            is PartialState.AddCaption -> {
                var map = currentState.imageCaptionMap
                if (map == null) {
                    map = HashMap()
                }
                map[currentState.selectedImage] = partialState.caption
                currentState.copy(imageCaptionMap = map, caption = partialState.caption)
            }

            is PartialState.CurrentImage -> {
                val caption = currentState.imageCaptionMap?.get(partialState.image)
                currentState.copy(selectedImage = partialState.image, caption = caption)
            }
            is PartialState.NoChange -> currentState
            is PartialState.SetImages -> {
                val map = currentState.imageCaptionMap
                map?.remove(partialState.captureImage)
                val currentImage = partialState.images[partialState.images.size - 1]
                val caption = map?.get(currentImage)

                currentState.copy(
                    imageList = partialState.images,
                    imageCaptionMap = map,
                    selectedImage = currentImage,
                    caption = caption
                )
            }
            is PartialState.Loading -> currentState.copy(isLoading = true)
            is PartialState.Error -> currentState.copy(isLoading = false)
            is PartialState.Saved -> currentState.copy(isLoading = false)
        }
    }
}
