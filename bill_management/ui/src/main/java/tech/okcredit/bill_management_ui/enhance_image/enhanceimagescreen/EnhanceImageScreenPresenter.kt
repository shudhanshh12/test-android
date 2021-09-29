package tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageScreenContract.*
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageScreenContract.PartialState.*
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Inject

class EnhanceImageScreenPresenter @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam("image_urls") val images: ArrayList<CapturedImage>?
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            intent<Intent.Load>()
                .map { SetImageURL(images!![0], true) },
            intent<Intent.UpdateImage>()
                .map {
                    val list = ArrayList<CapturedImage>()
                    images!!.forEachIndexed { index, capturedImage ->
                        if (index == 0) {
                            list.add(CapturedImage(it.it))
                        } else {
                            list.add(capturedImage)
                        }
                    }
                    UpdateImageProp(list, false)
                }
        )
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is SetImageURL -> currentState.copy(
                imageURL = partialState.imageURL,
                canEnhanceIamge = partialState.canEnhanceImage
            )
            is UpdateImageProp -> currentState.copy(
                imageList = partialState.list,
                canEnhanceIamge = partialState.canEnhanceImage
            )
        }
    }
}
