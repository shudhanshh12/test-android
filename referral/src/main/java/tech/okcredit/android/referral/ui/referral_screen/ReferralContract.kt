package tech.okcredit.android.referral.ui.referral_screen

import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ReferralContract {
    data class State(
        val isLoading: Boolean = false,
        val version: ReferralVersion = ReferralVersion.REWARDS_ON_ACTIVATION,
        val contextualHelpIds: List<String> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class SetReferralVersion(val version: ReferralVersion) : PartialState()

        object NoChange : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()
    }

    sealed class ReferralViewEvent : BaseViewEvent {

        object ShowTargetedUsersFragment : ReferralViewEvent()

        object ShowShareFragment : ReferralViewEvent()

        object NoReferralError : ReferralViewEvent()
    }
}
