package `in`.okcredit.frontend.ui.know_more._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.know_more.KnowMoreContract
import `in`.okcredit.frontend.ui.know_more.KnowMoreFragment
import `in`.okcredit.frontend.ui.know_more.KnowMoreViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class KnowMoreModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: KnowMoreFragment): KnowMoreContract.Navigator

    companion object {

        @Provides
        fun initialState(): KnowMoreContract.State = KnowMoreContract.State()

        @Provides
        @ViewModelParam("id")
        fun provideId(activity: MainActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_ID)
        }

        @Provides
        @ViewModelParam("account_type")
        fun provideTempNewNumber(activity: MainActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_ACCOUNT_TYPE)
        }

        @Provides
        fun viewModel(
            fragment: KnowMoreFragment,
            viewModelProvider: Provider<KnowMoreViewModel>
        ): MviViewModel<KnowMoreContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
