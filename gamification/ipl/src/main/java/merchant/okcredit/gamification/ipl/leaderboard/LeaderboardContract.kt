package merchant.okcredit.gamification.ipl.leaderboard

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.gamification.ipl.game.data.server.model.response.LeaderBoardResponse

interface LeaderboardContract {

    data class State(
        val isLoading: Boolean = true,
        val isPaginating: Boolean = false,
        val leaderBoardList: LeaderBoardResponse? = null,
        val networkError: Boolean = false,
        val paginationNetworkError: Boolean = false,
        val serverError: Boolean = false,
        val paginationServerError: Boolean = false,
        val hasMoreData: Boolean = false,
        val gameRulesCollapsed: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object ShowProgress : PartialState()
        object ShowPaginationProgress : PartialState()
        data class LeaderBoardDetails(val leaderBoardResponse: LeaderBoardResponse) : PartialState()
        data class LeaderBoardDetailsPaginate(val leaderBoardResponse: LeaderBoardResponse) : PartialState()
        object ShowNetworkError : PartialState()
        object ShowPaginationNetworkError : PartialState()
        object ShowServerError : PartialState()
        object ShowPaginationServerError : PartialState()
        object SetGameCardCollapsed : PartialState()
    }

    sealed class
    Intent : UserIntent {
        object Load : Intent()
        object Retry : Intent()
        object Paginate : Intent()
        object CollapseGameCard : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object NetworkErrorToast : ViewEvent()
        object ServerErrorToast : ViewEvent()
    }
}
