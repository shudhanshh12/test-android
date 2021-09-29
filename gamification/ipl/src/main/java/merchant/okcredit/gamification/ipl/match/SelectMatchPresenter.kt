package merchant.okcredit.gamification.ipl.match

import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.game.ui.youtube.usecase.GetYoutubeLink
import merchant.okcredit.gamification.ipl.game.usecase.GetActiveMatches
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.match.SelectMatchContract.*
import merchant.okcredit.gamification.ipl.match.SelectMatchContract.PartialState.*
import merchant.okcredit.gamification.ipl.match.usecase.GetAllIplRewards
import merchant.okcredit.gamification.ipl.match.usecase.GetMysteryPrizes
import merchant.okcredit.gamification.ipl.match.views.MatchControllerModel
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel
import merchant.okcredit.gamification.ipl.rewards.toControllerModel
import merchant.okcredit.gamification.ipl.utils.IplUtils
import javax.inject.Inject

class SelectMatchPresenter @Inject constructor(
    initialState: Lazy<State>,
    private val getActiveMatches: Lazy<GetActiveMatches>,
    private val getAllIplRewards: Lazy<GetAllIplRewards>,
    private val rewardsSyncer: Lazy<RewardsSyncer>,
    private val getMysteryPrizes: Lazy<GetMysteryPrizes>,
    private val eventTracker: Lazy<IplEventTracker>,
    private val getYoutubeLink: Lazy<GetYoutubeLink>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    private val matchModels = mutableListOf<MatchControllerModel>()
    private val rewardModels = mutableListOf<IplRewardsControllerModel>()
    private val mysteryPrizesModels = mutableListOf<IplRewardsControllerModel>()

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            getActiveMatches(),
            reloadActiveMatches(),
            syncRewards(),
            getRewards(),
            getMysteryPrizes(),
            collapseGameCard(),
            getYoutubeVideoUrl()
        )
    }

    private fun getYoutubeVideoUrl(): Observable<PartialState> {
        return intent<Intent.GetYoutubeLink>().switchMap {
            getYoutubeLink.get().execute(it.youtubeLinks)
        }.map {
            when (it) {
                is Result.Success -> {
                    SetYoutubeUrl(it.value)
                }
                else -> NoChange
            }
        }
    }

    private fun getActiveMatches(): Observable<PartialState> {
        return intent<Intent.Load>().take(1).compose(getActiveMatchesTransformer())
    }

    private fun getMysteryPrizes(): Observable<PartialState> {
        return intent<Intent.OnResume>()
            .switchMap { getMysteryPrizes.get().execute() }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (mysteryPrizesModels.isEmpty() && it.value.isNotEmpty()) {
                            eventTracker.get().mysteryPrizeViewed(IplEventTracker.Value.TODAYS_TAB_SCREEN)
                        }
                        mysteryPrizesModels.clear()
                        mysteryPrizesModels.addAll(it.value.map { prize -> prize.toControllerModel() })
                        UpdateModelsState
                    }
                    else -> NoChange
                }
            }
    }

    private fun reloadActiveMatches(): Observable<PartialState> {
        return intent<Intent.GetActiveMatches>().compose(getActiveMatchesTransformer())
    }

    private fun getActiveMatchesTransformer(): ObservableTransformer<Intent, PartialState> {
        return ObservableTransformer { upstream ->
            upstream.switchMap { getActiveMatches.get().execute() }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            matchModels.clear()
                            matchModels.add(MatchControllerModel.MatchLoadingModel)
                        }
                        is Result.Success -> {
                            eventTracker.get().matchScreenViewed()
                            pushIntent(Intent.GetYoutubeLink(it.value.youtubeLinks))
                            if (it.value.matches.isEmpty()) {
                                matchModels.clear()
                                matchModels.add(MatchControllerModel.NoActiveMatch)
                            } else {
                                matchModels.clear()
                                matchModels.addAll(
                                    it.value.matches.map { match ->
                                        MatchControllerModel.ActiveMatchModel(match)
                                    }
                                )
                            }
                        }
                        is Result.Failure -> {
                            matchModels.clear()
                            val isInternetIssue = isInternetIssue(it.error)
                            if (isInternetIssue) {
                                eventTracker.get().networkError(
                                    IplEventTracker.Value.TODAYS_TAB_SCREEN,
                                    IplUtils.getErrorCode(it.error),
                                    it.error.cause?.message
                                )
                            } else {
                                eventTracker.get().serverError(
                                    IplEventTracker.Value.TODAYS_TAB_SCREEN,
                                    IplUtils.getErrorCode(it.error),
                                    it.error.cause?.message
                                )
                            }
                            matchModels.add(MatchControllerModel.MatchLoadError(isInternetIssue))
                        }
                    }
                    emitViewEvent(ViewEvent.ShowTop)
                    UpdateModelsState
                }
        }
    }

    private fun syncRewards(): Observable<PartialState> {
        return intent<Intent.OnResume>()
            .switchMap { wrap(rxSingle { rewardsSyncer.get().syncRewards() }) }
            .map {
                NoChange
            }
    }

    private fun getRewards(): Observable<PartialState> {
        return getAllIplRewards.get().execute().map {
            when (it) {
                is Result.Success -> {
                    if (rewardModels.isEmpty() && it.value.isNotEmpty()) {
                        eventTracker.get().rewardsViewed(IplEventTracker.Value.TODAYS_TAB_SCREEN)
                    }
                    rewardModels.clear()
                    rewardModels.addAll(it.value.map { reward -> reward.toControllerModel() })
                    UpdateModelsState
                }
                else -> NoChange
            }
        }
    }

    private fun collapseGameCard(): Observable<PartialState> {
        return intent<Intent.CollapseGameCard>().map {
            SetGameCardCollapsed
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            NoChange -> currentState
            is SetYoutubeUrl -> currentState.copy(youtubeUrl = partialState.url)
            is SetGameCardCollapsed -> currentState.copy(
                models = matchModels + getRewardModel() + getGameRules(true)
            )
            is UpdateModelsState -> currentState.copy(
                models = matchModels + getRewardModel() + getGameRules(false)
            )
        }
    }

    private fun getRewardModel(): List<MatchControllerModel> {
        val error = matchModels.firstOrNull { it is MatchControllerModel.MatchLoadError }
        val models = rewardModels + mysteryPrizesModels
        return when {
            error != null -> emptyList()
            models.isEmpty() -> listOf(MatchControllerModel.RewardsModel(models))
            else -> listOf(MatchControllerModel.RewardsTitleModel, MatchControllerModel.RewardsModel(models))
        }
    }

    private fun getGameRules(collapsed: Boolean): List<MatchControllerModel> {
        val list = mutableListOf<MatchControllerModel>()
        val error = matchModels.firstOrNull { it is MatchControllerModel.MatchLoadError }
        if (error == null) {
            list.add(MatchControllerModel.GameRulesModel(collapsed))
        }

        return list
    }
}
