package tech.okcredit.home.ui._di

import `in`.okcredit.frontend.contract.FrontendConstants
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.ui.supplier_tab.SupplierTabContract
import tech.okcredit.home.ui.supplier_tab.SupplierTabFragment
import tech.okcredit.home.ui.supplier_tab.SupplierTabViewModel
import javax.inject.Provider

@Module
abstract class SupplierTabFragmentModule {

    @Binds
    @FragmentScope
    abstract fun listeners(fragment: SupplierTabFragment): SupplierTabContract.Listeners

    companion object {

        @Provides
        fun initialState(): SupplierTabContract.State = SupplierTabContract.State()

        @Provides
        fun viewModel(
            fragment: SupplierTabFragment,
            viewModelProvider: Provider<SupplierTabViewModel>
        ): MviViewModel<SupplierTabContract.State> = fragment.createViewModel(viewModelProvider)

        @Provides
        @ViewModelParam("notification_url")
        fun notificationUrl(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(FrontendConstants.EXTRA_NOTIFICATION)
                ?.takeUnless { it.isEmpty() }
                ?: ""
        }
    }
}
