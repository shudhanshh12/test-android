package tech.okcredit.account_chat_ui.message_layout

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SendMessageContract {
    data class State(
        val loading: Boolean = false,
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val sendButtonState: SendButtonState = SendButtonState.Inactive,
        val editTextState: EditTextState = EditTextState.Empty,
        val accountID: String? = null,
        val role: String? = null,
        val messageSentStatus: Boolean = false,
        val accountName: String? = null,
        val receiverRole: String? = null
    ) : UiState

    sealed class SendButtonState {
        object Inactive : SendButtonState()
        object Active : SendButtonState()
    }

    sealed class EditTextState {
        object Empty : EditTextState()
        object Filled : EditTextState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object TrackMessageStart : Intent()
        data class SendMessage(val preMessageState: PreMessageState) : Intent()
        data class LoadInitialData(val initialData: IntialData) : Intent()
        data class Message(val message: String) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        data class SendMessage(val message: String) : PartialState()
        object NoChange : PartialState()
        object ErrorState : PartialState()
        data class InitialData(val initialData: IntialData) : PartialState()
        data class SendButtonState(val sendButtonState: SendMessageContract.SendButtonState) : PartialState()
        data class PostMessageState(val status: Boolean, val editTextState: SendMessageContract.EditTextState) :
            PartialState()

        data class SetMessageSentState(val status: Boolean) : PartialState()
        data class LayoutInputState(
            val sendButtonState: SendMessageContract.SendButtonState,
            val editTextState: SendMessageContract.EditTextState
        ) : PartialState()
    }

    interface Interactor {
        fun postMessageSentActions()
    }

    interface Listener {
        fun gotoLogin()
    }

    data class IntialData(
        val accountID: String,
        val role: String?,
        val accountName: String?,
        val recevierRole: String?
    )

    data class PreMessageState(
        val message: String?,
        val accountID: String?,
        val role: String?,
        val accountName: String?,
        val recevierRole: String?
    )

    interface Callback {
        fun onMessageSent()
    }
}
