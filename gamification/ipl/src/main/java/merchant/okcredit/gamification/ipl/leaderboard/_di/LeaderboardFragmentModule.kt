package merchant.okcredit.gamification.ipl.leaderboard._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardLeaderboard
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardFragment
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardPresenter
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class LeaderboardFragmentModule {
    @Binds
    abstract fun leaderBoardGameRulesListener(fragment: LeaderboardFragment): GameRulesCardLeaderboard.OnGameRulesListener

    companion object {

        @Provides
        fun initialState(): LeaderboardContract.State = LeaderboardContract.State()

        @Provides
        fun viewModel(
            fragment: LeaderboardFragment,
            viewModelProvider: Provider<LeaderboardPresenter>,
        ): MviViewModel<LeaderboardContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
