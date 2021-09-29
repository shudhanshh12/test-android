package merchant.okcredit.gamification.ipl.game._di

import `in`.okcredit.merchant.contract.UpdateBusiness
import `in`.okcredit.referral.contract.usecase.GetShareAppIntent
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import merchant.okcredit.gamification.ipl.game.ui.GameActivity
import merchant.okcredit.gamification.ipl.game.ui.GameContract
import merchant.okcredit.gamification.ipl.game.ui.GameFragment
import merchant.okcredit.gamification.ipl.game.ui.GamePresenter
import merchant.okcredit.gamification.ipl.game.ui.epoxy.ItemLoadMore
import merchant.okcredit.gamification.ipl.game.ui.epoxy.batsman.ItemBatman
import merchant.okcredit.gamification.ipl.game.ui.epoxy.bowler.ItemBowlers
import merchant.okcredit.gamification.ipl.game.ui.youtube.usecase.GetYoutubeLink
import merchant.okcredit.gamification.ipl.game.usecase.*
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.IplRewardsController
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class GameFragmentModule {

    @Binds
    abstract fun batsmanListener(fragment: GameFragment): ItemBatman.BatsmanListener

    @Binds
    abstract fun loadMore(fragment: GameFragment): ItemLoadMore.LoadMoreListener

    @Binds
    abstract fun bowlersListener(fragment: GameFragment): ItemBowlers.BowlersListener

    companion object {

        @Provides
        fun initialState(): GameContract.State = GameContract.State()

        @Provides
        fun viewModel(
            fragment: GameFragment,
            viewModelProvider: Provider<GamePresenter>
        ): MviViewModel<GameContract.State> = fragment.createViewModel(viewModelProvider)

        @Provides
        fun iplRewardsController(eventTracker: IplEventTracker): IplRewardsController {
            return IplRewardsController(IplEventTracker.Value.GAME_SCREEN, eventTracker)
        }

        @Provides
        fun gamePresenter(
            activity: AppCompatActivity,
            initialState: Lazy<GameContract.State>,
            getOnboardingDetails: Lazy<GetOnboardingDetails>,
            selectTeam: Lazy<SelectTeam>,
            selectBatsman: Lazy<SelectBatsman>,
            selectBowler: Lazy<SelectBowler>,
            getTeamScore: Lazy<GetTeamScore>,
            getPlayerScore: Lazy<GetPlayerScore>,
            getBoosterQuestion: Lazy<GetBoosterQuestion>,
            getBusinessTypes: Lazy<GetBusinessTypes>,
            submitBoosterAnswer: Lazy<SubmitBoosterAnswer>,
            updateBusiness: Lazy<UpdateBusiness>,
            getMatchStatus: Lazy<GetMatchStatus>,
            getBoosterTrigger: Lazy<GetBoosterTrigger>,
            getPrediction: Lazy<GetPrediction>,
            shareAppIntent: Lazy<GetShareAppIntent>,
            eventTracker: Lazy<IplEventTracker>,
            addOkCreditContact: Lazy<AddOkCreditContact>,
            getYoutubeLink: Lazy<GetYoutubeLink>,
            firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
        ): GamePresenter {
            val matchId = activity.intent.getStringExtra(GameActivity.MATCH_ID)
            return GamePresenter(
                initialState,
                matchId,
                getOnboardingDetails,
                selectTeam,
                selectBatsman,
                selectBowler,
                getTeamScore,
                getPlayerScore,
                getBoosterQuestion,
                getBusinessTypes,
                submitBoosterAnswer,
                updateBusiness,
                getMatchStatus,
                getBoosterTrigger,
                getPrediction,
                shareAppIntent,
                eventTracker,
                addOkCreditContact,
                getYoutubeLink,
                firebaseRemoteConfig
            )
        }
    }
}
