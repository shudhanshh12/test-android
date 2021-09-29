package `in`.okcredit.merchant.rewards.ui.rewards_dialog

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyValue.ENTER_BANK_DETAILS_CTA_TEXT
import `in`.okcredit.analytics.PropertyValue.GO_TO_REWARDS_CTA_TEXT
import `in`.okcredit.analytics.PropertyValue.TELL_YOUR_FRIENDS_CTA_TEXT
import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.merchant.rewards.analytics.RewardsEventTracker
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsContract.*
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsContract.PartialState.*
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.ExecuteFeatureRewardsDeeplink
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.GetRewardShareIntent
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase.SetClaimErrorPreference
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardStatus
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardType.PAY_ONLINE_CASHBACK_REWARDS
import `in`.okcredit.rewards.contract.RewardsClaimHelper
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class ClaimRewardsViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(ClaimRewardActivity.EXTRA_REWARD) private val rewards: RewardModel,
    @ViewModelParam(ClaimRewardActivity.EXTRA_SOURCE) private val source: String,
    @ViewModelParam(ClaimRewardActivity.EXTRA_REFERENCE_ID) private val referenceId: String,
    private val rewardsClaimHelper: Lazy<RewardsClaimHelper>,
    private val getConnectionStatus: Lazy<GetConnectionStatus>,
    private val getCollectionActivationStatus: Lazy<GetCollectionActivationStatus>,
    private val internalDeeplinkNavigator: Lazy<ExecuteFeatureRewardsDeeplink>,
    private val setErrorPreference: Lazy<SetClaimErrorPreference>,
    private val rewardsEventTracker: Lazy<RewardsEventTracker>,
    private val getRewardShareIntent: Lazy<GetRewardShareIntent>,
) : BaseViewModel<State, PartialState, ViewEvents>(
    initialState
) {

    private var hasRevealReward = false

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            showScratchViewIfUnclaimed(),
            scratchViewPartiallyRevealed(),
            checkNetworkHealth(),
            revealReward(),
            claimRewards(),
            shareRewards(),
            goToRewardsScreen(),
            showMerchantDestinationDialog(),
            nudgeFeature(),
            checkCollectionStatus(),
            setClaimError(),
            finishActivity(),
        )
    }

    private fun checkNetworkHealth(): Observable<UiState.Partial<State>> {
        return intent<Intent.Load>()
            .switchMap { getConnectionStatus.get().execute() }
            .filter { it !is Result.Progress }
            .map {
                SetNetworkError(it !is Result.Success)
            }
    }

    private fun showScratchViewIfUnclaimed(): Observable<UiState.Partial<State>> {
        return intent<Intent.Load>().map {
            if (!hasRevealReward && rewards.isUnclaimed()) {
                rewardsEventTracker.get().trackRewardPopUpUnscratched(
                    screen = Event.REWARD_SCREEN,
                    type = RewardType.fromString(rewards.reward_type!!)!!.name,
                    rewardId = rewards.id,
                    source = source,
                    referenceId = referenceId,
                    paymentId = rewards.getLabelByKey(RewardModel.LabelKeys.PAYMENT_ID),
                    accountId = rewards.getLabelByKey(RewardModel.LabelKeys.ACCOUNT_ID),
                    collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                    createdBy = rewards.createdBy,
                )
                return@map SetScratchViewState(true)
            } else {
                pushIntent(Intent.RevealReward)
                return@map NoChange
            }
        }
    }

    private fun revealReward(): Observable<UiState.Partial<State>> {
        return intent<Intent.RevealReward>().map {
            if ((!hasRevealReward && !rewards.isClaimed())) {
                hasRevealReward = true
                if (rewards.amount > 0) {
                    pushIntent(Intent.CheckCollectionStatus)
                } else {
                    pushIntent(Intent.ClaimReward)
                }
            }
            rewardsEventTracker.get().trackRewardPopUpSeen(
                screen = Event.REWARD_SCREEN,
                type = RewardType.fromString(rewards.reward_type!!)!!.name,
                rewardId = rewards.id,
                source = source,
                referenceId = referenceId,
                paymentId = rewards.getLabelByKey(RewardModel.LabelKeys.PAYMENT_ID),
                accountId = rewards.getLabelByKey(RewardModel.LabelKeys.ACCOUNT_ID),
                collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                createdBy = rewards.createdBy,
                amount = rewards.amount
            )
            SetScratchViewState(false)
        }
    }

    private fun scratchViewPartiallyRevealed() = intent<Intent.ScratchViewPartiallyRevealed>()
        .map {
            rewardsEventTracker.get().trackRewardPopUpScratched(
                screen = Event.REWARD_SCREEN,
                type = RewardType.fromString(rewards.reward_type!!)!!.name,
                rewardId = rewards.id,
                source = source,
                referenceId = referenceId,
                paymentId = rewards.getLabelByKey(RewardModel.LabelKeys.PAYMENT_ID),
                accountId = rewards.getLabelByKey(RewardModel.LabelKeys.ACCOUNT_ID),
                collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                createdBy = rewards.createdBy,
            )
            SetScratchViewPartiallyRevealed
        }

    private fun nudgeFeature(): Observable<UiState.Partial<State>> {
        return intent<Intent.NudgeFeature>()
            .switchMap {
                internalDeeplinkNavigator.get().execute(it.deepLink)
            }.map {
                NoChange
            }
    }

    private fun shareRewards(): Observable<UiState.Partial<State>> {
        return intent<Intent.ShareReward>()
            .map {
                rewardsEventTracker.get().trackButtonClick(
                    screen = Event.REWARD_SCREEN,
                    value = TELL_YOUR_FRIENDS_CTA_TEXT,
                    type = RewardType.fromString(rewards.reward_type!!)!!.name,
                    source = source,
                    rewardId = rewards.id,
                    referenceId = referenceId,
                    collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                )
            }
            .switchMap { getRewardShareIntent.get().execute(rewards.amount) }
            .map {
                if (it is Result.Success) {
                    emitViewEvent(ViewEvents.ShareReward(it.value))
                }
                NoChange
            }
    }

    private fun goToRewardsScreen(): Observable<UiState.Partial<State>> {
        return intent<Intent.GoToRewardsScreen>()
            .map {
                rewardsEventTracker.get().trackButtonClick(
                    screen = Event.REWARD_SCREEN,
                    value = GO_TO_REWARDS_CTA_TEXT,
                    type = RewardType.fromString(rewards.reward_type!!)!!.name,
                    source = source,
                    rewardId = rewards.id,
                    referenceId = referenceId,
                    collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
                )
                emitViewEvent(ViewEvents.GoToRewardsScreen)
                NoChange
            }
    }

    private fun checkCollectionStatus(): Observable<PartialState> {
        return intent<Intent.CheckCollectionStatus>()
            .switchMap { wrap(getCollectionActivationStatus.get().execute()) }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (it.value) {
                            pushIntent(Intent.ClaimReward)
                        }
                        if (RewardType.fromString(
                                rewards.reward_type
                                    ?: ""
                            ) == PAY_ONLINE_CASHBACK_REWARDS
                        ) {
                            SetEnterBankDetailsButtonState(!it.value)
                        } else {
                            SetEnterBankDetailsButtonState(false)
                        }
                    }
                    else -> NoChange
                }
            }
    }

    private fun claimRewards(): Observable<UiState.Partial<State>> {
        return intent<Intent.ClaimReward>()
            .switchMap { wrap(rewardsClaimHelper.get().claim(rewards.id)) }
            .map {
                val helper = rewardsClaimHelper.get()
                when (it) {
                    is Result.Progress -> ClaimInProgressState
                    is Result.Success -> {
                        emitViewEvent(ViewEvents.DebugClaimStatus("ClaimStatus : ${it.value.status}"))
                        when (val status = it.value) {
                            is RewardStatus.CLAIMED -> {
                                rewardsEventTracker.get().trackClaimRewardState(status.status)
                                rewardsEventTracker.get().trackClaimReward(
                                    screen = Event.REWARD_SCREEN,
                                    amount = rewards.amount,
                                    type = rewards.reward_type
                                )
                                ClaimSuccessState
                            }
                            is RewardStatus.PROCESSING -> {
                                rewardsEventTracker.get().trackClaimRewardState(status.status)
                                when {
                                    helper.isPayoutDelayed(status) -> ClaimProcessPayoutDelayedState
                                    helper.isPayoutStarted(status) -> ClaimProcessPayoutStartedState
                                    helper.isBudgetExhausted(status) -> ClaimProcessBudgetExhaustedState
                                    else -> ClaimSuccessState
                                }
                            }
                            is RewardStatus.ON_HOLD -> {
                                rewardsEventTracker.get().trackClaimRewardState(status.status)
                                when {
                                    helper.isBankDetailsDuplication(status) -> ClaimOnHoldBankDetailsDuplicationState
                                    helper.isUpiInactive(status) -> ClaimOnHoldUpiInactiveState
                                    helper.isDailyPayoutLimitReached(status) -> ClaimOnHoldDailyLimitReachedState
                                    else -> {
                                        emitViewEvent(ViewEvents.ClaimFailure)
                                        NoChange
                                    }
                                }
                            }
                            is RewardStatus.FAILED -> {
                                rewardsEventTracker.get().trackClaimRewardState(status.status)
                                when {
                                    helper.isFailedBankUnavailable(status) -> ClaimFailedBankUnavailableState
                                    else -> {
                                        emitViewEvent(ViewEvents.ClaimFailure)
                                        NoChange
                                    }
                                }
                            }
                            else -> {
                                emitViewEvent(ViewEvents.ClaimFailure)
                                NoChange
                            }
                        }
                    }
                    is Result.Failure -> {
                        emitViewEvent(ViewEvents.DebugClaimStatus("Failure : ${it.error}"))
                        rewardsEventTracker.get().trackClaimRewardState("Generic Error")
                        if (isInternetIssue(it.error)) {
                            SetNetworkError(true)
                        } else {
                            emitViewEvent(ViewEvents.ClaimFailure)
                            NoChange
                        }
                    }
                }
            }
    }

    private fun finishActivity(): Observable<UiState.Partial<State>> {
        return intent<Intent.FinishActivity>()
            .map {
                rewardsEventTracker.get().trackRewardPopUpDismissed(
                    screen = Event.REWARD_SCREEN,
                    type = RewardType.fromString(rewards.reward_type!!)!!.name,
                    rewardId = rewards.id,
                    source = source,
                    referenceId = referenceId,
                    paymentId = rewards.getLabelByKey(RewardModel.LabelKeys.PAYMENT_ID),
                    accountId = rewards.getLabelByKey(RewardModel.LabelKeys.ACCOUNT_ID),
                    createdBy = rewards.createdBy,
                )
                emitViewEvent(ViewEvents.FinishActivity)
                NoChange
            }
    }

    private fun setClaimError(): Observable<UiState.Partial<State>> {
        return intent<Intent.SetError>()
            .map {
                setErrorPreference.get().execute(status = true)
                NoChange
            }
    }

    private fun showMerchantDestinationDialog(): Observable<PartialState> {
        return intent<Intent.SetBankDetails>().map {
            rewardsEventTracker.get().trackButtonClick(
                screen = Event.REWARD_SCREEN,
                value = ENTER_BANK_DETAILS_CTA_TEXT,
                type = RewardType.fromString(rewards.reward_type!!)!!.name,
                rewardId = rewards.id,
                source = source,
                referenceId = referenceId,
                collectionStatus = getCollectionActivationStatus.get().execute().blockingFirst(),
            )
            emitViewEvent(
                ViewEvents.ShowAddMerchantDestinationDialog()
            )
            NoChange
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            is ClaimInProgressState -> currentState.copy(
                claimApiProcessingState = true
            )
            is ClaimSuccessState -> currentState.copy(
                claimSuccess = true,
                claimed = true,
                claimApiProcessingState = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isFailedBankUnavailable = false,
                isUpiInactive = false,
                isPayoutDelayed = false,
                isDailyPayoutLimitReached = false,
            )
            is SetNetworkError -> currentState.copy(
                networkError = partialState.status
            )
            is ClaimOnHoldBankDetailsDuplicationState -> currentState.copy(
                isBankDetailsDuplication = true,
                claimApiProcessingState = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false,
                isFailedBankUnavailable = false
            )
            is ClaimProcessBudgetExhaustedState -> currentState.copy(
                isBudgetExhausted = true,
                claimApiProcessingState = false,
                isBankDetailsDuplication = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false,
                isFailedBankUnavailable = false
            )
            is ClaimProcessPayoutDelayedState -> currentState.copy(
                isPayoutDelayed = true,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false,
                isFailedBankUnavailable = false
            )
            is ClaimProcessPayoutStartedState -> currentState.copy(
                isPayoutInitiated = true,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false,
                isFailedBankUnavailable = false
            )
            is ClaimOnHoldUpiInactiveState -> currentState.copy(
                isUpiInactive = true,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isDailyPayoutLimitReached = false,
                isFailedBankUnavailable = false
            )
            ClaimOnHoldDailyLimitReachedState -> currentState.copy(
                isDailyPayoutLimitReached = true,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isFailedBankUnavailable = false
            )
            ClaimFailedBankUnavailableState -> currentState.copy(
                isFailedBankUnavailable = true,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false
            )
            is ClaimProcessCustomMessage -> currentState.copy(
                isProcessingCustomMessageAvailable = true,
                customMessages = partialState.customMessages,
                isFailedBankUnavailable = false,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false
            )
            is ClaimOnHoldCustomMessage -> currentState.copy(
                isOnHoldCustomMessageAvailable = true,
                customMessages = partialState.customMessages,
                isFailedBankUnavailable = false,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false
            )
            is ClaimFailedCustomMessage -> currentState.copy(
                isFailedCustomMessageAvailable = true,
                customMessages = partialState.customMessages,
                isFailedBankUnavailable = false,
                claimApiProcessingState = false,
                networkError = false,
                isBankDetailsDuplication = false,
                isBudgetExhausted = false,
                isPayoutDelayed = false,
                isPayoutInitiated = false,
                isUpiInactive = false,
                isDailyPayoutLimitReached = false
            )
            is SetScratchViewState -> currentState.copy(
                showScratchView = partialState.isVisible,
            )
            is SetScratchViewPartiallyRevealed -> currentState.copy(
                isScratchViewPartiallyRevealed = true
            )
            is SetEnterBankDetailsButtonState -> currentState.copy(
                showEnterBankDetailsButton = partialState.isVisible
            )
        }
    }
}
