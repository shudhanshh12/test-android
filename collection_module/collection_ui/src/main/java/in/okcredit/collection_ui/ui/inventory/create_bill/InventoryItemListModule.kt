package `in`.okcredit.collection_ui.ui.inventory.create_bill

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class InventoryItemListModule {

    companion object {

        @Provides
        fun initialState(): InventoryItemListContract.State = InventoryItemListContract.State()

        @Provides
        fun viewModel(
            fragment: InventoryItemListFragment,
            viewModelProvider: Provider<InventoryItemListViewModel>
        ): MviViewModel<InventoryItemListContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
