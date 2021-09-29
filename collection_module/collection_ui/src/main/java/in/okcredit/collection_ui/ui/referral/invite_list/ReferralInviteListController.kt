package `in`.okcredit.collection_ui.ui.referral.invite_list

import `in`.okcredit.collection_ui.ui.referral.invite_list.views.TargetedReferralInviteListItem
import `in`.okcredit.collection_ui.ui.referral.invite_list.views.targetedReferralHeaderView
import `in`.okcredit.collection_ui.ui.referral.invite_list.views.targetedReferralInviteView
import `in`.okcredit.collection_ui.ui.referral.invite_list.views.targetedReferralListHeading
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class ReferralInviteListController @Inject
constructor(private val fragment: ReferralInviteListFragment) :
    TypedEpoxyController<List<TargetedReferralInviteListItem>>() {

    override fun buildModels(data: List<TargetedReferralInviteListItem>?) {
        data?.forEach { data ->
            when (data) {
                is TargetedReferralInviteListItem.CustomerReferralItem -> {
                    targetedReferralInviteView {
                        id(data.targetedCustomerReferralInfo.id)
                        referralInfo(data.targetedCustomerReferralInfo)
                        listener(fragment)
                    }
                }
                is TargetedReferralInviteListItem.ListHeadingItem -> {
                    targetedReferralListHeading {
                        id("header list")
                        comingFromLedger(data.comingFrmLedger)
                    }
                }

                is TargetedReferralInviteListItem.HeaderViewItem -> {
                    targetedReferralHeaderView {
                        id("header view")
                    }
                }
            }
        }
    }
}
