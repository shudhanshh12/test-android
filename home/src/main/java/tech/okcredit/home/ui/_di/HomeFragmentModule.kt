package tech.okcredit.home.ui._di

import `in`.okcredit.frontend.contract.FrontendConstants
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.ui.home.HomeContract
import tech.okcredit.home.ui.home.HomeFragment
import tech.okcredit.home.ui.home.HomeViewModel
import javax.inject.Provider

@Module
abstract class HomeFragmentModule {

    companion object {
        @Provides
        fun initialState(): HomeContract.State = HomeContract.State()

        @Provides
        @ViewModelParam(FrontendConstants.ARG_SHOW_COLLECTION_POPUP)
        fun openOneTapCollectionPopup(activity: AppCompatActivity): Boolean {
            return activity.intent.getBooleanExtra(FrontendConstants.ARG_SHOW_COLLECTION_POPUP, false)
        }

        @Provides
        @ViewModelParam(FrontendConstants.ARG_SHOW_INAPP_REVIEW)
        fun openReviewDialog(activity: AppCompatActivity): Boolean {
            return activity.intent.getBooleanExtra(FrontendConstants.ARG_SHOW_INAPP_REVIEW, false)
        }

        @Provides
        @ViewModelParam(FrontendConstants.ARG_SHOW_BULK_REMINDER)
        fun openBulkReminder(activity: AppCompatActivity): Boolean {
            return activity.intent.getBooleanExtra(FrontendConstants.ARG_SHOW_BULK_REMINDER, false)
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: HomeFragment,
            viewModelProvider: Provider<HomeViewModel>,
        ): MviViewModel<HomeContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
