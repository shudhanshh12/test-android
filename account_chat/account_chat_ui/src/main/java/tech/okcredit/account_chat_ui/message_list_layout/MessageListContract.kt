package tech.okcredit.account_chat_ui.message_list_layout

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface MessageListContract {
    data class State(
        val loading: Boolean = false,
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val sendButtonState: SendButtonState = SendButtonState.Inactive,
        val editTextState: EditTextState = EditTextState.Empty,
        val token: String = "",
        val accountID: String? = null,
        val merchatId: String? = null,
        val role: String? = null,
        val uid: String? = null,
        val unreadMessageCount: String? = null,
        val firstUnreadMessageId: String? = null
    ) : UiState

    sealed class SendButtonState {
        object Inactive : SendButtonState()
        object Active : SendButtonState()
    }

    sealed class EditTextState {
        object Empty : EditTextState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class LoadInitialData(val initialData: IntialData) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        data class SendMessage(val message: String) : PartialState()
        object NoChange : PartialState()
        object ErrorState : PartialState()
        data class InitialData(val initialData: IntialData) : PartialState()
        data class EditTextState(val state: MessageListContract.EditTextState) : PartialState()
        data class SendButtonState(val sendButtonState: MessageListContract.SendButtonState) : PartialState()
        data class SetBusiness(val business: Business?) : PartialState()
        data class SetCurrentUserId(val uid: String) : PartialState()
    }

    interface Interactor

    interface Listener {
        fun gotoLogin()
    }

    data class IntialData(
        val accountID: String,
        val unreadMessageCount: String?,
        val firstUnreadMessageId: String?
    )

    interface Callback {
        fun isMessageListEmpty(isEmpty: Boolean)
    }
}
