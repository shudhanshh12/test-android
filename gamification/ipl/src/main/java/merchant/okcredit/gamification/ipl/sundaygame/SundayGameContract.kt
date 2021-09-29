package merchant.okcredit.gamification.ipl.sundaygame

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.gamification.ipl.game.data.server.model.response.SundayGameResponse
import merchant.okcredit.gamification.ipl.game.data.server.model.response.YoutubeLinks
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel

interface SundayGameContract {

    data class State(
        val isLoading: Boolean = true,
        val cardList: List<SundayGameModel>? = null,
        val networkError: Boolean = false,
        val serverError: Boolean = false,
        val progressCardState: ProgressCardState? = null,
        val rewards: List<IplRewardsControllerModel> = listOf(),
        val sundayGameResponse: SundayGameResponse? = null,
        val youtubeUrl: String = "",
    ) : UiState

    data class ProgressCardState(
        val progress: Int = 0,
        val totalPoints: Int = 0,
        val completedSteps: Int = 0,
    )

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object ShowProgress : PartialState()
        data class SundayGame(val sundayGameResponse: SundayGameResponse) : PartialState()
        object ShowNetworkError : PartialState()
        object ShowServerError : PartialState()
        object SetGameCardCollapsed : PartialState()
        object RewardsState : PartialState()
        data class SetYoutubeUrl(val url: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object CollapseGameCard : Intent()
        data class GetYoutubeLink(val youtubeLinks: List<YoutubeLinks>) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object NetworkErrorToast : ViewEvent()
        object ServerErrorToast : ViewEvent()
    }
}
