package merchant.okcredit.gamification.ipl.view._di

import `in`.okcredit.shared.base.MviViewModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.view.IplContract
import merchant.okcredit.gamification.ipl.view.IplFragment
import merchant.okcredit.gamification.ipl.view.IplViewModel
import merchant.okcredit.gamification.ipl.view.IplViewPagerAdapter
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class IplFragmentModule {

    companion object {

        @Provides
        fun initialState() = IplContract.State()

        @Provides
        fun viewModel(
            fragment: IplFragment,
            viewModelProvider: Provider<IplViewModel>,
        ): MviViewModel<IplContract.State> = fragment.createViewModel(viewModelProvider)

        @Provides
        fun iplViewPagerAdapter(fragment: IplFragment, firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>) =
            IplViewPagerAdapter(fragment, firebaseRemoteConfig)
    }
}
