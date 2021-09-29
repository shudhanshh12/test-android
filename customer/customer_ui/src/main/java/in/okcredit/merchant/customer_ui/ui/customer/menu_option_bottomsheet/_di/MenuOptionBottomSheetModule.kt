package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet._di

import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionBottomSheetContract
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionBottomSheetViewModel
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.model.MenuSheet
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class MenuOptionBottomSheetModule {

    companion object {

        @Provides
        fun initialState(fragment: MenuOptionsBottomSheet): MenuOptionBottomSheetContract.State {
            val menuSheet = fragment.requireArguments().getParcelable<MenuSheet>(MenuOptionsBottomSheet.MENU_PARCEL)
            return MenuOptionBottomSheetContract.State(menuSheet = menuSheet)
        }

        @Provides
        fun viewModel(
            fragment: MenuOptionsBottomSheet,
            viewModelProvider: Provider<MenuOptionBottomSheetViewModel>
        ): MviViewModel<MenuOptionBottomSheetContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
