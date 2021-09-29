package merchant.okcredit.gamification.ipl.rewards.mysteryprize

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeContract.*
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeContract.PartialState.ClaimSuccessState
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeContract.PartialState.NoChange
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.usecase.ClaimMysteryPrize

class ClaimMysteryPrizePresenter constructor(
    initialState: Lazy<State>,
    private val prize: MysteryPrizeModel,
    private val source: String,
    private val claimMysteryPrize: Lazy<ClaimMysteryPrize>,
    private val eventTracker: Lazy<IplEventTracker>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            loadRewardType(),
            load(),
            claimPrize()
        )
    }

    private fun loadRewardType(): Observable<PartialState> {
        return intent<Intent.Load>().take(1).map {
            PartialState.WelcomeReward(prize.isWelcomeReward())
        }
    }

    private fun load(): Observable<PartialState> {
        return intent<Intent.Load>().take(1).map {
            if (!prize.isClaimed()) {
                pushIntent(Intent.ClaimPrize)
            }
            NoChange
        }
    }

    private fun claimPrize(): Observable<PartialState> {
        return intent<Intent.ClaimPrize>().switchMap {
            claimMysteryPrize.get().execute(prize.id)
        }.map {
            when (it) {
                is Result.Progress -> PartialState.ClaimProgressState
                is Result.Success -> {
                    eventTracker.get().mysteryPrizeClaimed(source)
                    emitViewEvent(ViewEvent.RewardWon)
                    ClaimSuccessState
                }
                is Result.Failure -> {
                    if (isInternetIssue(it.error)) {
                        emitViewEvent(ViewEvent.InternetIssue)
                    } else {
                        emitViewEvent(ViewEvent.ServerError)
                    }
                    NoChange
                }
            }
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is PartialState.WelcomeReward -> currentState.copy(welcomeReward = partialState.welcomeReward)
            ClaimSuccessState -> currentState.copy(claimed = true, claimInProgress = false)
            PartialState.ClaimProgressState -> currentState.copy(claimed = true, claimInProgress = true)
        }
    }
}
