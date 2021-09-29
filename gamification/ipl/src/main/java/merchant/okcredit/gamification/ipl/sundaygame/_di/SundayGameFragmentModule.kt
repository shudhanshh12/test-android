package merchant.okcredit.gamification.ipl.sundaygame._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardWeekly
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameFragment
import merchant.okcredit.gamification.ipl.sundaygame.SundayGamePresenter
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SundayGameFragmentModule {

    @Binds
    abstract fun gameRulesListener(fragment: SundayGameFragment): GameRulesCardWeekly.OnGameRulesListener

    companion object {

        @Provides
        fun initialState(): SundayGameContract.State = SundayGameContract.State()

        @Provides
        fun viewModel(
            fragment: SundayGameFragment,
            viewModelProvider: Provider<SundayGamePresenter>
        ): MviViewModel<SundayGameContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
