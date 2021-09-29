package `in`.okcredit.collection_ui.ui.inventory.items

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class InventoryItemModule {

    companion object {

        @Provides
        fun initialState(): InventoryItemContract.State = InventoryItemContract.State()

        @Provides
        fun viewModel(
            fragment: InventoryItemFragment,
            viewModelProvider: Provider<InventoryItemViewModel>
        ): MviViewModel<InventoryItemContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
