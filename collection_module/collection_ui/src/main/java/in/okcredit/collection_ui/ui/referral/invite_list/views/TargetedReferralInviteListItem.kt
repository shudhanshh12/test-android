package `in`.okcredit.collection_ui.ui.referral.invite_list.views

import `in`.okcredit.collection.contract.TargetedCustomerReferralInfo

sealed class TargetedReferralInviteListItem {
    data class CustomerReferralItem(
        val targetedCustomerReferralInfo: TargetedCustomerReferralInfo,
    ) : TargetedReferralInviteListItem()

    data class ListHeadingItem(
        val comingFrmLedger: Boolean = false,
    ) : TargetedReferralInviteListItem()

    data class HeaderViewItem(
        val text: String? = null,
    ) : TargetedReferralInviteListItem()
}
