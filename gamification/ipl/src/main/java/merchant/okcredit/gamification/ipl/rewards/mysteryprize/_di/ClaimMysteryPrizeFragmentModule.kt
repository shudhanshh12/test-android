package merchant.okcredit.gamification.ipl.rewards.mysteryprize._di

import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardActivity
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeContract
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeFragment
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizePresenter
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.usecase.ClaimMysteryPrize
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class ClaimMysteryPrizeFragmentModule {

    companion object {

        @Provides
        fun initialState(activity: AppCompatActivity): ClaimMysteryPrizeContract.State {
            val prize = activity.intent.getParcelableExtra<MysteryPrizeModel>(ClaimRewardActivity.EXTRA_MYSTERY_PRIZE)
            return ClaimMysteryPrizeContract.State(prize.isClaimed(), prize.amount)
        }

        @Provides
        fun claimRewardPresenter(
            activity: AppCompatActivity,
            state: Lazy<ClaimMysteryPrizeContract.State>,
            claimMysteryPrize: Lazy<ClaimMysteryPrize>,
            eventTracker: Lazy<IplEventTracker>
        ): ClaimMysteryPrizePresenter {
            val prize = activity.intent.getParcelableExtra<MysteryPrizeModel>(ClaimRewardActivity.EXTRA_MYSTERY_PRIZE)
            val source = activity.intent.getStringExtra(ClaimRewardActivity.EXTRA_SOURCE)
            return ClaimMysteryPrizePresenter(state, prize, source, claimMysteryPrize, eventTracker)
        }

        @Provides
        fun viewModel(
            fragment: ClaimMysteryPrizeFragment,
            viewModelProvider: Provider<ClaimMysteryPrizePresenter>
        ): MviViewModel<ClaimMysteryPrizeContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
