package `in`.okcredit.onboarding.businessname

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class BusinessNameModule {

    @Binds
    abstract fun navigator(fragment: BusinessNameFragment): BusinessNameContract.Navigator

    companion object {

        @Provides
        fun initialState(): BusinessNameContract.State = BusinessNameContract.State()

        @Provides
        fun viewModel(
            fragment: BusinessNameFragment,
            viewModelProvider: Provider<BusinessNameViewModel>
        ): MviViewModel<BusinessNameContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
