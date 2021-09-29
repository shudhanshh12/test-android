package `in`.okcredit.collection_ui.ui.inventory.bills

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class InventoryBillModule {

    companion object {

        @Provides
        fun initialState(): InventoryBillContract.State = InventoryBillContract.State()

        @Provides
        fun viewModel(
            fragment: InventoryBillFragment,
            viewModelProvider: Provider<InventoryBillViewModel>
        ): MviViewModel<InventoryBillContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
