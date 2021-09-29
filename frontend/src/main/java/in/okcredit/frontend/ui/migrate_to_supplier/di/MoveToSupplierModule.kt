package `in`.okcredit.frontend.ui.migrate_to_supplier.di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.migrate_to_supplier.MoveToSupplierContract
import `in`.okcredit.frontend.ui.migrate_to_supplier.MoveToSupplierFragment
import `in`.okcredit.frontend.ui.migrate_to_supplier.MoveToSupplierViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class MoveToSupplierModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: MoveToSupplierFragment): MoveToSupplierContract.Navigator

    companion object {

        @Provides
        @ViewModelParam(MainActivity.ARG_CUSTOMER_ID)
        fun customerId(activity: MainActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_CUSTOMER_ID)
        }

        @Provides
        fun initialState(): MoveToSupplierContract.State = MoveToSupplierContract.State()

        @Provides
        fun viewModel(
            fragment: MoveToSupplierFragment,
            viewModelProvider: Provider<MoveToSupplierViewModel>
        ): MviViewModel<MoveToSupplierContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
