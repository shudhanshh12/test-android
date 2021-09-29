package tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageFragment
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageScreenContract
import tech.okcredit.bill_management_ui.enhance_image.enhanceimagescreen.EnhanceImageScreenPresenter
import tech.okcredit.camera_contract.CapturedImage
import javax.inject.Provider

@Module
abstract class EnhanceImageFragmentModule {

    companion object {

        @Provides
        fun initialState(): EnhanceImageScreenContract.State = EnhanceImageScreenContract.State()

        @Provides
        fun viewModel(
            fragment: EnhanceImageFragment,
            viewModelProvider: Provider<EnhanceImageScreenPresenter>
        ): MviViewModel<EnhanceImageScreenContract.State> = fragment.createViewModel(viewModelProvider)

        @Provides
        @ViewModelParam("image_urls")
        fun getImageURL(enhanceImageFragment: EnhanceImageFragment): ArrayList<CapturedImage> {
            return enhanceImageFragment.activity?.intent?.getSerializableExtra("addedImages") as ArrayList<CapturedImage>
        }
    }
}
