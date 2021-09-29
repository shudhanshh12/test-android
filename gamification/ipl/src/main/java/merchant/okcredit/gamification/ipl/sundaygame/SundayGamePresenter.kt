package merchant.okcredit.gamification.ipl.sundaygame

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableSource
import merchant.okcredit.gamification.ipl.game.data.server.model.response.SundayGameResponse
import merchant.okcredit.gamification.ipl.game.ui.youtube.usecase.GetYoutubeLink
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel
import merchant.okcredit.gamification.ipl.rewards.toControllerModel
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract.*
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract.Intent.CollapseGameCard
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract.Intent.Load
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract.PartialState.*
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel
import merchant.okcredit.gamification.ipl.sundaygame.usecase.GetSundayGameData
import merchant.okcredit.gamification.ipl.sundaygame.usecase.GetWeeklyIplRewards
import merchant.okcredit.gamification.ipl.utils.IplUtils
import javax.inject.Inject
import kotlin.math.min

class SundayGamePresenter @Inject constructor(
    initialState: Lazy<State>,
    private val getSundayGameData: Lazy<GetSundayGameData>,
    private val getWeeklyIplRewards: Lazy<GetWeeklyIplRewards>,
    private val eventTracker: Lazy<IplEventTracker>,
    private val getYoutubeLink: Lazy<GetYoutubeLink>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    private val rewardModels = mutableListOf<IplRewardsControllerModel>()

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            getSundayGameDetails(),
            getWeeklyRewards(),
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

    private fun getSundayGameDetails(): ObservableSource<PartialState> {
        return intent<Load>()
            .switchMap { getSundayGameData.get().execute() }
            .map {
                when (it) {
                    is Result.Success -> {
                        eventTracker.get().weeklyDrawViewed()
                        pushIntent(Intent.GetYoutubeLink(it.value.youtubeLinks))
                        SundayGame(it.value)
                    }
                    is Result.Progress -> ShowProgress
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                eventTracker.get().networkError(
                                    IplEventTracker.Value.SUNDAY_TAB_SCREEN,
                                    IplUtils.getErrorCode(it.error),
                                    it.error.cause?.message
                                )
                                ShowNetworkError
                            }
                            else -> {
                                eventTracker.get().serverError(
                                    IplEventTracker.Value.SUNDAY_TAB_SCREEN,
                                    IplUtils.getErrorCode(it.error),
                                    it.error.cause?.message
                                )
                                ShowServerError
                            }
                        }
                    }
                }
            }
    }

    private fun getWeeklyRewards(): Observable<PartialState> {
        return getWeeklyIplRewards.get().execute().map {
            when (it) {
                is Result.Success -> {
                    if (rewardModels.isEmpty() && it.value.isNotEmpty()) {
                        eventTracker.get().rewardsViewed(IplEventTracker.Value.SUNDAY_TAB_SCREEN)
                    }
                    rewardModels.clear()
                    rewardModels.addAll(it.value.map { reward -> reward.toControllerModel() })
                    RewardsState
                }
                else -> NoChange
            }
        }
    }

    private fun collapseGameCard(): Observable<PartialState> {
        return intent<CollapseGameCard>().map {
            SetGameCardCollapsed
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is SetYoutubeUrl -> currentState.copy(youtubeUrl = partialState.url)
            is ShowProgress -> currentState.copy(
                isLoading = currentState.cardList.isNullOrEmpty() // we show progress only if current list is empty
            )
            is SundayGame -> currentState.copy(
                isLoading = false,
                cardList = transformDetails(
                    partialState.sundayGameResponse,
                    rulesCollapsed = true,
                    serverError = false
                ),
                progressCardState = getProgressStateData(partialState.sundayGameResponse),
                sundayGameResponse = partialState.sundayGameResponse,
                networkError = false,
                serverError = false
            )
            is ShowNetworkError -> {
                if (currentState.cardList.isNullOrEmpty()) {
                    currentState.copy(
                        isLoading = false,
                        networkError = true
                    )
                } else {
                    emitViewEvent(ViewEvent.NetworkErrorToast)
                    currentState.copy(
                        isLoading = false
                    )
                }
            }
            is ShowServerError -> {
                if (currentState.cardList.isNullOrEmpty()) {
                    currentState.copy(
                        isLoading = false,
                        serverError = true
                    )
                } else {
                    emitViewEvent(ViewEvent.ServerErrorToast)
                    currentState.copy(
                        isLoading = false
                    )
                }
            }

            is RewardsState -> currentState.copy(
                cardList = transformDetails(
                    currentState.sundayGameResponse,
                    rulesCollapsed = false,
                    currentState.serverError
                )
            )
            is SetGameCardCollapsed -> currentState.copy(
                cardList = transformDetails(
                    currentState.sundayGameResponse,
                    rulesCollapsed = true,
                    currentState.serverError
                )
            )
        }
    }

    private fun getProgressStateData(sundayGameResponse: SundayGameResponse): ProgressCardState? {
        val totalSteps = sundayGameResponse.threshold.boosterCount + 1
        var currentStep =
            min(sundayGameResponse.threshold.boosterCount, sundayGameResponse.merchantStats.boosterCount)
        if (sundayGameResponse.pendingPoints() <= 0) {
            currentStep++
        }

        val progress = ((currentStep * 100) / totalSteps)

        return ProgressCardState(progress, sundayGameResponse.merchantStats.points, currentStep)
    }

    private fun transformDetails(
        sundayGameResponse: SundayGameResponse? = null,
        rulesCollapsed: Boolean,
        serverError: Boolean,
    ): List<SundayGameModel> {
        val list = mutableListOf<SundayGameModel>()

        val rewards = rewardModels.toList()
        if (sundayGameResponse?.morePlay == true) {
            list.add(SundayGameModel.PlayAgain)
        }

        if (rewards.isNullOrEmpty().not() && !serverError) {
            list.add(SundayGameModel.Rewards(rewards))
        }

        // check if user has already qualified
        if (sundayGameResponse != null) {
            when {
                sundayGameResponse.isQualifiedForDraw() -> {

                    list.add(
                        SundayGameModel.LuckyDrawQualifiedCard(
                            runs = sundayGameResponse.merchantStats.points,
                            endTime = sundayGameResponse.dayDifference(),
                            date = (sundayGameResponse.expiryTime * 1_000)
                        )
                    )
                }
                else -> list.add(
                    SundayGameModel.PendingBoosterCard(
                        sundayGameResponse.getBoosterStatus(),
                        endTime = sundayGameResponse.dayDifference(),
                        pendingRuns = sundayGameResponse.pendingPoints(),
                        runs = sundayGameResponse.merchantStats.points,
                        threadHoldBooster = sundayGameResponse.threshold.boosterCount,
                        threadHoldRuns = sundayGameResponse.threshold.points,
                        isRunsCompleted = sundayGameResponse.isRunsCompleted()
                    )
                )
            }
            // add sunday game rule
            if (sundayGameResponse != null) {
                list.add(
                    SundayGameModel.GameRules(
                        boosterCount = sundayGameResponse.threshold.boosterCount,
                        totalRuns = sundayGameResponse.threshold.points,
                        date = (sundayGameResponse.expiryTime * 1_000),
                        rulesCollapsed = rulesCollapsed
                    )
                )
            }
        }
        return list
    }
}
