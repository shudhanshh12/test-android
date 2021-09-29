package `in`.okcredit.sales_ui.ui.billing_name._di

import `in`.okcredit.sales_ui.ui.billing_name.BillingNameBottomSheetDialog
import `in`.okcredit.sales_ui.ui.billing_name.BillingNameContract
import `in`.okcredit.sales_ui.ui.billing_name.BillingNameViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class BillingNameModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: BillingNameBottomSheetDialog): BillingNameContract.Navigator

    companion object {

        @Provides
        fun initialState(): BillingNameContract.State = BillingNameContract.State()

        @Provides
        fun viewModel(
            fragment: BillingNameBottomSheetDialog,
            viewModelProvider: Provider<BillingNameViewModel>
        ): MviViewModel<BillingNameContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
