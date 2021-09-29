package tech.okcredit.android.referral.ui.know_more

import `in`.okcredit.referral.contract.models.ReferralInfo
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ReferralKnowMoreContract {

    data class State(
        val version: ReferralVersion = ReferralVersion.REWARDS_ON_ACTIVATION,
        val referralInfo: ReferralInfo? = null,
        val totalClaimedReferralRewards: Long = 0,
        val totalUnclaimedReferralRewards: Long = 0
    ) : UiState {

        val totalRewards
            get() = totalClaimedReferralRewards + totalUnclaimedReferralRewards
    }

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SetReferralVersion(val version: ReferralVersion) : PartialState()

        data class SetReferralInfo(val referralInfo: ReferralInfo) : PartialState()

        data class SetTotalRewards(val totalClaimedReferralRewards: Long, val totalUnclaimedReferralRewards: Long) :
            PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object GoToReferralRewards : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToReferralRewards : ViewEvent()

        data class ShowCollectionDialog(val unclaimedRewards: Long) : ViewEvent()
    }
}
