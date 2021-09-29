package tech.okcredit.android.referral.ui.share

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ShareReferralContract {

    data class State(
        val showProgress: Boolean = false,
        val showNudge: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object ShowProgress : PartialState()
        object HideProgress : PartialState()
        object ShowNudge : PartialState()
        object HideNudge : PartialState()
        object NoChange : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShareToWhatsApp(val intent: android.content.Intent) : ViewEvent()

        object ShareFailure : ViewEvent()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object WhatsAppShare : Intent()
    }
}
