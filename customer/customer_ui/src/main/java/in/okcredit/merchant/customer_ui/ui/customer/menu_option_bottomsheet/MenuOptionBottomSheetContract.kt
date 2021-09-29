package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.model.MenuSheet
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface MenuOptionBottomSheetContract {
    data class State(
        val menuSheet: MenuSheet? = null,
        val canShowInviteOption: Boolean = false,
        val referralVersion: ReferralVersion = ReferralVersion.NO_REWARD,
        val showHiddenMenuOptions: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class ShowInviteOption(val canShow: Boolean) : PartialState()
        data class SetReferralVersion(val referralVersion: ReferralVersion) : PartialState()
        data class ShowHiddenMenuOptions(val show: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class CustomerModel(
            val customer: Customer? = null
        ) : Intent()

        data class SendInviteToWhatsApp(
            val targetUser: TargetedUser
        ) : Intent()

        object MoreOptionClicked : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class SendInviteToTargetedUser(
            val shareIntent: android.content.Intent
        ) : ViewEvent()
    }
}
