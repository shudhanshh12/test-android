package merchant.okcredit.gamification.ipl.match

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.gamification.ipl.game.data.server.model.response.YoutubeLinks
import merchant.okcredit.gamification.ipl.match.views.MatchControllerModel

interface SelectMatchContract {

    data class State(
        val models: List<MatchControllerModel> = listOf(
            MatchControllerModel.MatchLoadingModel,
            MatchControllerModel.GameRulesModel(true)
        ),
        val collapse: Boolean = false,
        val youtubeUrl: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object SetGameCardCollapsed : PartialState()

        object UpdateModelsState : PartialState()

        data class SetYoutubeUrl(val url: String) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object OnResume : Intent()

        object CollapseGameCard : Intent()

        object GetActiveMatches : Intent()

        data class GetYoutubeLink(val youtubeLinks: List<YoutubeLinks>) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowTop : ViewEvent()
    }
}
