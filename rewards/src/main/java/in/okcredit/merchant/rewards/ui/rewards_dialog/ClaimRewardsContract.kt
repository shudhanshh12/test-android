package `in`.okcredit.merchant.rewards.ui.rewards_dialog

import `in`.okcredit.analytics.Event
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardType.Companion.fromString
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import org.joda.time.DateTime
import tech.okcredit.android.base.TempCurrencyUtil

interface ClaimRewardsContract {

    data class FeatureDetails(
        val featureTitle: String = "",
        val featureDescription: String = "",
        val featureIcon: String = "",
        val deepLink: String = "",
    )

    data class State(
        val displayAmount: String,
        val claimed: Boolean,
        val rewardType: RewardType,
        val createTime: DateTime,
        val claimApiProcessingState: Boolean = false,
        val claimSuccess: Boolean = false,
        val networkError: Boolean = false,
        val isPayoutInitiated: Boolean = false,
        val isPayoutDelayed: Boolean = false,
        val isBankDetailsDuplication: Boolean = false,
        val isBudgetExhausted: Boolean = false,
        val isUpiInactive: Boolean = false,
        val isDailyPayoutLimitReached: Boolean = false,
        val isFailedBankUnavailable: Boolean = false,
        val isFeatureReward: Boolean = false,
        val featureDetails: FeatureDetails? = null,
        val isBetterLuckNextTimeReward: Boolean = false,
        val customMessages: String = "",
        val isProcessingCustomMessageAvailable: Boolean = false,
        val isOnHoldCustomMessageAvailable: Boolean = false,
        val isFailedCustomMessageAvailable: Boolean = false,
        val showScratchView: Boolean = false,
        val isScratchViewPartiallyRevealed: Boolean = false,
        val showEnterBankDetailsButton: Boolean = false,
        val canShowGoToRewardsButton: Boolean = false,
    ) : UiState {

        companion object {

            fun fromReward(reward: RewardModel): State {
                return State(
                    displayAmount = TempCurrencyUtil.formatV2(reward.amount),
                    claimed = reward.isClaimed(),
                    createTime = reward.create_time,
                    rewardType = fromString(reward.reward_type ?: "") ?: RewardType.REWARD_TYPE_UNKNOWN,
                    isBetterLuckNextTimeReward = fromString(
                        reward.reward_type ?: ""
                    ) == RewardType.BETTER_LUCK_NEXT_TIME,
                    isFeatureReward = reward.isFeatureRewards(),

                    featureDetails = reward.takeIf { it.isFeatureRewards() }?.run {
                        FeatureDetails(
                            featureTitle = featureTitle,
                            featureIcon = icon,
                            featureDescription = description,
                            deepLink = deepLink
                        )
                    }
                )
            }
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetScratchViewState(val isVisible: Boolean) : PartialState()

        object SetScratchViewPartiallyRevealed : PartialState()

        data class SetEnterBankDetailsButtonState(val isVisible: Boolean) : PartialState()

        object ClaimSuccessState : PartialState()

        object ClaimProcessPayoutDelayedState : PartialState()
        object ClaimProcessPayoutStartedState : PartialState()
        object ClaimProcessBudgetExhaustedState : PartialState()
        data class ClaimProcessCustomMessage(val customMessages: String) : PartialState()

        object ClaimOnHoldBankDetailsDuplicationState : PartialState()
        object ClaimOnHoldUpiInactiveState : PartialState()
        object ClaimOnHoldDailyLimitReachedState : PartialState()
        data class ClaimOnHoldCustomMessage(val customMessages: String) : PartialState()

        object ClaimFailedBankUnavailableState : PartialState()
        data class ClaimFailedCustomMessage(val customMessages: String) : PartialState()

        object ClaimInProgressState : PartialState()

        data class SetNetworkError(val status: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object RevealReward : Intent()

        object ScratchViewPartiallyRevealed : Intent()

        object ClaimReward : Intent()

        object ShareReward : Intent()

        object SetBankDetails : Intent()

        object GoToRewardsScreen : Intent()

        data class NudgeFeature(val deepLink: String) : Intent()

        object CheckCollectionStatus : Intent()

        object SetError : Intent()

        object FinishActivity : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {

        data class DebugClaimStatus(
            val status: String,
        ) : ViewEvents()

        object ClaimFailure : ViewEvents()

        data class ShareReward(val intent: android.content.Intent) : ViewEvents()

        object GoToRewardsScreen : ViewEvents()

        data class ShowAddMerchantDestinationDialog(
            val isUpdateCollection: Boolean = false,
            val paymentMethodType: String? = null,
            val source: String? = Event.REWARD_SCREEN,
        ) : ViewEvents()

        object FinishActivity : ViewEvents()
    }
}
