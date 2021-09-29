package tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselContract
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselFragment
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselViewModel
import javax.inject.Provider

@Module
abstract class VideoBackedCarouselModule {

    companion object {
        @Provides
        fun viewModel(
            fragment: VideoBackedCarouselFragment,
            viewModelProvider: Provider<VideoBackedCarouselViewModel>,
        ): MviViewModel<VideoBackedCarouselContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
