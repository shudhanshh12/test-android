package tech.okcredit.home.ui.home.supplier

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SupplierSortModule {

    companion object {

        @Provides
        fun initialState(): SupplierSortContract.State = SupplierSortContract.State()

        @Provides
        fun viewModel(
            fragment: SupplierSortFragment,
            viewModelProvider: Provider<SupplierSortViewModel>
        ): MviViewModel<SupplierSortContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
