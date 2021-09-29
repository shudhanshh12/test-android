package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen._di

import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.CategoryFragment
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.CategoryScreenContract
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.CategoryScreenViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class CategoryFragmentModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: CategoryFragment): CategoryScreenContract.Navigator

    companion object {

        @Provides
        fun initialState(): CategoryScreenContract.State = CategoryScreenContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: CategoryFragment,
            viewModelProvider: Provider<CategoryScreenViewModel>
        ): MviViewModel<CategoryScreenContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
