package `in`.okcredit.merchant.rewards.ui.rewards_screen.di_

import `in`.okcredit.merchant.rewards.ui.rewards_screen.RewardsContract
import `in`.okcredit.merchant.rewards.ui.rewards_screen.RewardsFragment
import `in`.okcredit.merchant.rewards.ui.rewards_screen.RewardsViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class RewardsFragmentModule {

    companion object {

        @Provides
        fun initialState(): RewardsContract.State = RewardsContract.State()

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: RewardsFragment,
            viewModelProvider: Provider<RewardsViewModel>
        ): MviViewModel<RewardsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
