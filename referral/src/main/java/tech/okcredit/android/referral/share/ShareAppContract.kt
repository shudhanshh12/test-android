package tech.okcredit.android.referral.share

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import javax.inject.Inject

object ShareAppContract {

    data class State(
        val showProgressBar: Boolean = false,
        val canShowReferralDescription: Boolean = false,
    ) : UiState {

        @Inject
        constructor() : this(false)
    }

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object ShowProgressBar : PartialState()

        object HideProgressBar : PartialState()

        data class CanShowReferralDescription(val canShow: Boolean) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class ShareApp(val value: android.content.Intent) : ViewEvent()

        object ShareAppFailure : ViewEvent()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object ShareOnWhatsApp : Intent()
    }
}
