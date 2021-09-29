package `in`.okcredit.supplier.supplier_profile_bottom_sheet.di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileBottomSheet
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileBottomSheet.Companion.ARG_SUPPLIER_ID_PROFILE_PAGE
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileContract
import `in`.okcredit.supplier.supplier_profile_bottom_sheet.SupplierProfileViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class SupplierProfileModule {

    companion object {

        @Provides
        @ViewModelParam(ARG_SUPPLIER_ID_PROFILE_PAGE)
        fun supplierIdEProfilePage(fragment: SupplierProfileBottomSheet): String {
            return fragment.requireArguments().getString(ARG_SUPPLIER_ID_PROFILE_PAGE, "")
        }

        @Provides
        fun initialState(): SupplierProfileContract.State {
            return SupplierProfileContract.State()
        }

        @Provides
        fun viewModel(
            fragment: SupplierProfileBottomSheet,
            viewModelProvider: Provider<SupplierProfileViewModel>
        ): MviViewModel<SupplierProfileContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
