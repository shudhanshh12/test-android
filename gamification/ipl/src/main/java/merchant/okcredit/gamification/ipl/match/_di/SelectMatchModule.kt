package merchant.okcredit.gamification.ipl.match._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardNew
import merchant.okcredit.gamification.ipl.match.SelectMatchContract
import merchant.okcredit.gamification.ipl.match.SelectMatchFragment
import merchant.okcredit.gamification.ipl.match.SelectMatchPresenter
import merchant.okcredit.gamification.ipl.match.views.MatchLoadErrorView
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SelectMatchModule {

    @Binds
    abstract fun matchGameRulesListener(fragment: SelectMatchFragment): GameRulesCardNew.OnGameRulesListener

    companion object {

        @Provides
        fun initialState(): SelectMatchContract.State = SelectMatchContract.State()

        @Provides
        fun retryListener(fragment: SelectMatchFragment): MatchLoadErrorView.RetryListener = fragment

        @Provides
        fun viewModel(
            fragment: SelectMatchFragment,
            viewModelProvider: Provider<SelectMatchPresenter>
        ): MviViewModel<SelectMatchContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
