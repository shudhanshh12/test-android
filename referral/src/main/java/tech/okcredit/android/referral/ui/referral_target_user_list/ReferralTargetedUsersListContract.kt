package tech.okcredit.android.referral.ui.referral_target_user_list

import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

object ReferralTargetedUsersListContract {
    data class State constructor(
        val showProgress: Boolean = false,
        val genericShareEnabled: Boolean = false,
        val showGenericShare: Boolean = false,
        val targetedUsers: List<TargetedUser>? = null,
        // If value is null, we haven't got the the data yet. We don't know yet
        val showTargetedUserList: Boolean? = null
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()

        object ShareReferral : Intent()

        data class SendInviteToWhatsApp(
            val targetUser: TargetedUser
        ) : Intent()

        data class GoToReferralRewardsForPhoneNumber(
            val phoneNumber: String
        ) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object ShowProgress : PartialState()

        object HideProgress : PartialState()

        data class TargetedUsers(val targetedUsers: List<TargetedUser>) : PartialState()

        data class ShowGenericShare(val enabled: Boolean) : PartialState()
    }

    sealed class ViewEvents : BaseViewEvent {

        data class ReferralIntent(val shareIntent: android.content.Intent) : ViewEvents()

        object ShareFailure : ViewEvents()

        data class GoToReferralRewardsForPhoneNumber(
            val phoneNumber: String
        ) : ViewEvents()
    }
}
