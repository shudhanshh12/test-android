package `in`.okcredit.collection_ui.ui.insights

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class CollectionInsightsModule {

    @Binds
    abstract fun navigator(fragment: CollectionInsightsActivity): CollectionInsightsContract.Navigator

    companion object {

        @Provides
        fun initialState(): CollectionInsightsContract.State = CollectionInsightsContract.State()

        @Provides
        fun viewModel(
            fragment: CollectionInsightsActivity,
            viewModelProvider: Provider<CollectionInsightsViewModel>,
        ): MviViewModel<CollectionInsightsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
