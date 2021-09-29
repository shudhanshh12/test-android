package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ReferralInAppContract {
    object State : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    object ViewEvent : BaseViewEvent
}
