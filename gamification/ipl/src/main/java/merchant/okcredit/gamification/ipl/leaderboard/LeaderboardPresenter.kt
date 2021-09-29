package merchant.okcredit.gamification.ipl.leaderboard

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import merchant.okcredit.gamification.ipl.game.data.server.model.response.LeaderBoardResponse
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.*
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.Intent.CollapseGameCard
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.Intent.Load
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.PartialState.*
import merchant.okcredit.gamification.ipl.leaderboard.usercase.GetLeaderBoardDetails
import merchant.okcredit.gamification.ipl.utils.IplUtils
import javax.inject.Inject

class LeaderboardPresenter @Inject constructor(
    initialState: Lazy<State>,
    private val getLeaderBoardDetails: Lazy<GetLeaderBoardDetails>,
    private val eventTracker: Lazy<IplEventTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    private var key = ""
    private var hasMoreData = false
    private var leaderBoardResponse: LeaderBoardResponse? = null

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            loadLeaderBoardDetails(),
            retryLeaderBoardDetails(),
            paginateLeaderBoardDetails(),
            collapseGameCard()
        )
    }

    private fun loadLeaderBoardDetails(): ObservableSource<PartialState> {
        return intent<Load>()
            .take(1)
            .compose(loadLeaderBoard())
    }

    private fun retryLeaderBoardDetails(): ObservableSource<PartialState> {
        return intent<Intent.Retry>()
            .compose(loadLeaderBoard())
    }

    private fun paginateLeaderBoardDetails(): ObservableSource<PartialState> {
        return intent<Intent.Paginate>()
            .compose(loadLeaderBoard(true))
    }

    private fun loadLeaderBoard(pagination: Boolean = false): ObservableTransformer<Intent, PartialState> {
        return ObservableTransformer { upstream ->
            upstream.switchMap { getLeaderBoardDetails.get().execute(key) }
                .map {
                    when (it) {
                        is Result.Progress -> if (pagination) {
                            ShowPaginationProgress
                        } else {
                            ShowProgress
                        }
                        is Result.Success -> {
                            key = it.value.key
                            hasMoreData = it.value.hasMoreData()
                            if (pagination) {
                                LeaderBoardDetailsPaginate(transformList(pagination, it.value))
                            } else {
                                LeaderBoardDetails(transformList(pagination, it.value))
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    eventTracker.get().networkError(
                                        IplEventTracker.Value.LEADERBOARD_SCREEN,
                                        IplUtils.getErrorCode(it.error),
                                        it.error.cause?.message
                                    )
                                    if (pagination) {
                                        ShowPaginationNetworkError
                                    } else {
                                        ShowNetworkError
                                    }
                                }
                                else -> {
                                    eventTracker.get().serverError(
                                        IplEventTracker.Value.LEADERBOARD_SCREEN,
                                        IplUtils.getErrorCode(it.error),
                                        it.error.cause?.message
                                    )
                                    if (pagination) {
                                        ShowPaginationServerError
                                    } else {
                                        ShowServerError
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun transformList(
        paginate: Boolean,
        response: LeaderBoardResponse,
    ): LeaderBoardResponse {
        if (paginate) {
            val list = leaderBoardResponse?.topItems as MutableList
            list.addAll(response.topItems)
        } else {
            leaderBoardResponse = response
        }
        return leaderBoardResponse as LeaderBoardResponse
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
            is SetGameCardCollapsed -> currentState.copy(gameRulesCollapsed = true)
            is ShowProgress -> currentState.copy(
                isLoading = true,
                isPaginating = false
            )
            is ShowPaginationProgress -> currentState.copy(
                isLoading = false,
                isPaginating = true
            )
            is LeaderBoardDetails -> currentState.copy(
                isLoading = false,
                isPaginating = false,
                hasMoreData = hasMoreData,
                leaderBoardList = partialState.leaderBoardResponse,
                networkError = false,
                paginationNetworkError = false,
                serverError = false,
                paginationServerError = false
            )

            is LeaderBoardDetailsPaginate -> currentState.copy(
                isLoading = false,
                isPaginating = false,
                hasMoreData = hasMoreData,
                leaderBoardList = partialState.leaderBoardResponse,
                networkError = false,
                paginationNetworkError = false,
                serverError = false,
                paginationServerError = false
            )

            is ShowNetworkError -> currentState.copy(
                isLoading = false,
                networkError = true,
                serverError = false
            )

            is ShowPaginationNetworkError -> currentState.copy(
                isLoading = false,
                networkError = false,
                serverError = false,
                isPaginating = false,
                paginationNetworkError = true,
                paginationServerError = false
            )

            is ShowPaginationServerError -> currentState.copy(
                isLoading = false,
                networkError = false,
                serverError = false,
                isPaginating = false,
                paginationNetworkError = false,
                paginationServerError = true
            )

            is ShowServerError -> currentState.copy(
                isLoading = false,
                networkError = false,
                serverError = true
            )
        }
    }
}
