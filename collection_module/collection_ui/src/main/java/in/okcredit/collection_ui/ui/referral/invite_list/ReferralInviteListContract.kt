package `in`.okcredit.collection_ui.ui.referral.invite_list

import `in`.okcredit.collection.contract.TargetedCustomerReferralInfo
import `in`.okcredit.collection_ui.ui.referral.invite_list.views.TargetedReferralInviteListItem
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ReferralInviteListContract {

    data class State(
        val error: String = "",
        val referralList: List<TargetedCustomerReferralInfo> = arrayListOf(),
        val customerIdFrmLedger: String? = null,
        val customerInfoFrmLedger: TargetedCustomerReferralInfo? = null,
        val rewardAmount: Long = 0,
        val rewardBtnActive: Boolean = false,
        val list: List<TargetedReferralInviteListItem> = mutableListOf(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetReferralInfoList(
            val list: List<TargetedCustomerReferralInfo>,
            val customerInfoFrmLedger: TargetedCustomerReferralInfo?,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object Resume : Intent()
        data class InviteOnWhatsApp(val targetedCustomerReferralInfo: TargetedCustomerReferralInfo) : Intent()
        object GotoRewardScreen : Intent()
        data class ShareTargetedReferral(val customerMerchantId: String) : Intent()
        object HelpClicked : Intent()
        data class OnInviteBtnClicked(val targetedCustomerReferralInfo: TargetedCustomerReferralInfo) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        data class InviteOnWhatsApp(val intent: android.content.Intent) : ViewEvents()
        object GotoRewardScreen : ViewEvents()
        object ShowWhatsAppError : ViewEvents()
        data class HelpClicked(val ids: List<String>) : ViewEvents()
        object ShowSomethingWrongError : ViewEvents()
    }
}
