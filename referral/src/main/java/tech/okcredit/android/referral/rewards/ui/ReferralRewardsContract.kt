package tech.okcredit.android.referral.rewards.ui

import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes
import tech.okcredit.android.referral.data.ReferredMerchant

object ReferralRewardsContract {

    data class State(
        val earnings: String = "",
        val unclaimedRewards: String? = null,
        val showProgressBar: Boolean = true,
        @StringRes val toastMessage: Int? = null,
        val referredMerchants: List<ReferredMerchant> = emptyList(),
        val showError: Boolean = false,
        @StringRes val errorMessage: Int? = null,
        val referralVersion: ReferralVersion = ReferralVersion.REWARDS_ON_ACTIVATION
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        data class MerchantNotified(val referredMerchants: List<ReferredMerchant>) : PartialState()

        data class LoadCompleted(
            val earnings: String,
            val unclaimedRewards: String?,
            val referredMerchants: List<ReferredMerchant>
        ) : PartialState()

        data class ShowMessage(@StringRes val message: Int) : PartialState()

        data class Error(@StringRes val message: Int) : PartialState()

        object HideMessage : PartialState()

        object ShowProgress : PartialState()

        object NoChange : PartialState()

        data class setReferralVersion(val referralVersion: ReferralVersion) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class Notify(val phoneNumber: String) : Intent()

        object HideToastMessage : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class DeferredScrollToPos(val pos: Int) : ViewEvent()
    }
}
