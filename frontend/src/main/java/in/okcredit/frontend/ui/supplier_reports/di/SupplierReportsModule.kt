package `in`.okcredit.frontend.ui.supplier_reports.di

import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract
import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsFragment
import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SupplierReportsModule {

    companion object {

        @Provides
        fun initialState(): SupplierReportsContract.State = SupplierReportsContract.State()

        @Provides
        @ViewModelParam("supplier_id")
        fun supplierId(fragment: SupplierReportsFragment): String {
            return fragment.arguments?.getString("supplier_id") ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: SupplierReportsFragment,
            viewModelProvider: Provider<SupplierReportsViewModel>
        ): MviViewModel<SupplierReportsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
