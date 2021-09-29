package tech.okcredit.help.help_details

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.userSupport.data.LikeState
import tech.okcredit.userSupport.model.HelpItem

interface HelpDetailsContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val helpItemV2: HelpItem? = null,
        val likeState: LikeState = LikeState.NORMAL,
        val sourceScreen: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class isHelpItemFeedBackLike(val isLiked: Boolean) : PartialState()

        data class SetHelperItemDetail(val helpItemV2: HelpItem) : PartialState()

        data class setSourceScreen(val source: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object OnLikeClick : Intent()

        object OnDisLikeClick : Intent()

        data class SubmitFeedback(val feedback: String, val rating: Int) : Intent()
    }
    sealed class ViewEvent : BaseViewEvent {

        object GotoLogin : ViewEvent()

        object GoBack : ViewEvent()

        object GoBackAfterAnimation : ViewEvent()
    }
}
