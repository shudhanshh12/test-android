package tech.okcredit.feedback.feedback._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.feedback.feedback.FeedbackContract
import tech.okcredit.feedback.feedback.FeedbackFragment
import tech.okcredit.feedback.feedback.FeedbackViewModel
import javax.inject.Provider

@Module
abstract class FeedbackModule {

    companion object {

        @Provides
        fun initialState(): FeedbackContract.State = FeedbackContract.State()

        @Provides
        fun viewModel(
            fragment: FeedbackFragment,
            viewModelProvider: Provider<FeedbackViewModel>
        ): MviViewModel<FeedbackContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
