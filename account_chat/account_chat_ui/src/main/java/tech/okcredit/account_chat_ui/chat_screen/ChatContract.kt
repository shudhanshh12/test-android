package tech.okcredit.account_chat_ui.chat_screen

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.graphics.Bitmap

interface ChatContract {
    data class State(
        val isLoading: Boolean = false,
        val accountId: String? = null,
        val accountName: String? = null,
        val accountPic: String? = null,
        val token: String? = null,
        val role: String? = null,
        val recevierRole: String? = null,
        val error: Boolean = false,
        val isRegistered: Boolean = true,
        val mobile: String? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val unreadMessageCount: String? = null,
        val firstUnseenMessageId: String? = null,
        val canShowChatTooltip: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SetAccountId(val accountId: String, val token: String) : PartialState()

        data class SetRole(val role: String) :
            PartialState()

        data class SetAccount(
            val accountId: String?,
            val name: String?,
            val rofileImage: String?,
            val receiverRole: String,
            val isRegistered: Boolean,
            val mobile: String?,
            val unreadMesssageCount: String?,
            val firstUnseenMessageId: String?
        ) : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        data class SetIntentExtras(
            val role: String,
            val accountID: String,
            val unreadMesssageCount: String?,
            val firstUnseenMessageId: String?
        ) : PartialState()

        data class CanShowChatTooltip(val canShowChatTooltip: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object GoToPhoneDialer : Intent()
        data class ShareAppPromotion(val bitmap: Bitmap, val sharingText: String) : Intent()
        data class PageViewed(val screen: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoLogin : ViewEvent()
        data class OpenWhatsAppPromotionShare(val intent: android.content.Intent) : ViewEvent()
        data class GotoCallCustomer(val mobile: String?) : ViewEvent()
    }
}
