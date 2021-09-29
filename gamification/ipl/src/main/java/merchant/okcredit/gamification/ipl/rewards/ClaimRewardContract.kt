package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.android.base.TempCurrencyUtil

interface ClaimRewardContract {

    enum class RewardClaimErrorState {
        PROCESSING_PAYOUT_DELAYED,
        PROCESSING_PAYOUT_STARTED,
        PROCESSING_BUDGET_EXHAUSTED,
        PROCESSING_CUSTOM_MESSAGE,
        ON_HOLD_BANK_DETAILS_DUPLICATE,
        ON_HOLD_UPI_INACTIVE,
        ON_HOLD_DAILY_LIMIT_REACHED,
        ON_HOLD_CUSTOM_MESSAGE,
        FAILED_BANK_UNAVAILABLE,
        FAILED_BANK_CUSTOM_MESSAGE,
    }

    data class State(
        val displayAmount: String,
        val zeroReward: Boolean,
        val claimed: Boolean,
        val rewardType: RewardType,
        val merchantAddress: String? = null,
        val inAppChatEnabled: Boolean = false,
        val claimInProgress: Boolean = false,
        val nonCashReward: Boolean = false,
        val rewardClaimErrorState: RewardClaimErrorState? = null,
        val customerMessage: String? = null,
        val canShowScratchView: Boolean = true
    ) : UiState {

        companion object {

            fun fromReward(reward: RewardModel): State {
                return State(
                    displayAmount = TempCurrencyUtil.formatV2(reward.amount),
                    zeroReward = reward.amount == 0L,
                    claimed = reward.isClaimed(),
                    rewardType = RewardType.fromString(reward.reward_type ?: "") ?: RewardType.IPL_WEEKLY
                )
            }
        }
    }

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object ClaimSuccessState : PartialState()

        data class ClaimErrorState(val claimErrorState: RewardClaimErrorState, val customMessage: String? = null) :
            PartialState()

        object ClaimProgressState : PartialState()

        object ClaimFailureState : PartialState()

        data class MerchantAddress(val address: String?) : PartialState()

        data class InAppChatEnabled(val enabled: Boolean) : PartialState()

        data class NonCashReward(val nonCashReward: Boolean) : PartialState()

        data class SetShowScratchView(val canShow: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object ClaimReward : Intent()

        object Reveal : Intent()

        object RevealCashReward : Intent()

        object RevealNonCashReward : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class DebugClaimStatus(
            val status: String
        ) : ViewEvent()

        data class ShowAddPaymentDetailsDialog(val amount: Long) : ViewEvent()

        object Congratulate : ViewEvent()

        object NonCashRewardWon : ViewEvent()

        object InternetIssue : ViewEvent()

        object ServerError : ViewEvent()
    }
}
