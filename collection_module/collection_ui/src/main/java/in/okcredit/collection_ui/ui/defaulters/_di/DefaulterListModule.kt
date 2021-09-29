package `in`.okcredit.collection_ui.ui.defaulters._di

import `in`.okcredit.collection_ui.ui.defaulters.DefaulterController
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListActivity
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListContract
import `in`.okcredit.collection_ui.ui.defaulters.DefaulterListViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class DefaulterListModule {

    companion object {

        @Provides
        fun defaulterController(activity: DefaulterListActivity) = DefaulterController(activity)

        @Provides
        fun initialState(): DefaulterListContract.State = DefaulterListContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            activity: DefaulterListActivity,
            viewModelProvider: Provider<DefaulterListViewModel>
        ): MviViewModel<DefaulterListContract.State> = activity.createViewModel(viewModelProvider)
    }
}
