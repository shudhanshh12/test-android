package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardStatus
import `in`.okcredit.rewards.contract.RewardsClaimHelper
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract.*
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract.Intent.*
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract.PartialState.*
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract.RewardClaimErrorState.*
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.usecase.GetMerchantAddress
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class ClaimRewardPresenter @Inject constructor(
    initialState: Lazy<State>,
    @ViewModelParam(ClaimRewardActivity.EXTRA_REWARD) private val reward: RewardModel,
    @ViewModelParam(ClaimRewardActivity.EXTRA_SOURCE) private val source: String,
    private val getMerchantAddress: Lazy<GetMerchantAddress>,
    private val getCollectionActivationStatus: Lazy<GetCollectionActivationStatus>,
    private val eventTracker: Lazy<IplEventTracker>,
    private val rewardsClaimHelper: Lazy<RewardsClaimHelper>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            claimReward(),
            revealReward(),
            revealCashReward(),
            revealNonCashReward(),
            getMerchantAddress(),
            isNonCashReward(),
            canShowScratchView()
        )
    }

    private fun canShowScratchView() = intent<Load>()
        .map {
            if (reward.isUnclaimed()) {
                SetShowScratchView(true)
            } else {
                pushIntent(Reveal)
                SetShowScratchView(false)
            }
        }

    private fun claimReward(): Observable<PartialState> {
        return intent<ClaimReward>().switchMap {
            wrap(rewardsClaimHelper.get().claim(reward.id))
        }.map {
            val helper = rewardsClaimHelper.get()
            when (it) {
                is Result.Progress -> ClaimProgressState
                is Result.Success -> {
                    emitViewEvent(ViewEvent.DebugClaimStatus("ClaimStatus : ${it.value.status}"))
                    if (reward.isNonCashReward() || reward.amount > 0) {
                        emitViewEvent(ViewEvent.Congratulate)
                    }
                    eventTracker.get().rewardClaimed(source, reward.reward_type ?: "")
                    if (reward.amount > 0) {
                        when (val status = it.value) {
                            is RewardStatus.CLAIMED -> ClaimSuccessState
                            is RewardStatus.PROCESSING -> {
                                when {
                                    helper.isPayoutDelayed(status) -> ClaimErrorState(PROCESSING_PAYOUT_DELAYED)
                                    helper.isPayoutStarted(status) -> ClaimErrorState(PROCESSING_PAYOUT_STARTED)
                                    helper.isBudgetExhausted(status) -> ClaimErrorState(PROCESSING_BUDGET_EXHAUSTED)
                                    helper.isCustom(status) -> ClaimErrorState(
                                        PROCESSING_CUSTOM_MESSAGE,
                                        status.customMessage
                                    )
                                    else -> ClaimSuccessState
                                }
                            }
                            is RewardStatus.ON_HOLD -> {
                                when {
                                    helper.isBankDetailsDuplication(status) -> ClaimErrorState(
                                        ON_HOLD_BANK_DETAILS_DUPLICATE
                                    )
                                    helper.isUpiInactive(status) -> ClaimErrorState(ON_HOLD_UPI_INACTIVE)
                                    helper.isDailyPayoutLimitReached(status) -> ClaimErrorState(
                                        ON_HOLD_DAILY_LIMIT_REACHED
                                    )
                                    helper.isCustom(status) -> ClaimErrorState(
                                        ON_HOLD_CUSTOM_MESSAGE,
                                        status.customMessage
                                    )
                                    else -> ClaimFailureState
                                }
                            }
                            is RewardStatus.FAILED -> {
                                emitViewEvent(ViewEvent.DebugClaimStatus("ClaimStatus : ${it.value.status}"))
                                when {
                                    helper.isFailedBankUnavailable(status) -> ClaimErrorState(
                                        FAILED_BANK_UNAVAILABLE
                                    )
                                    helper.isCustom(status) -> ClaimErrorState(
                                        FAILED_BANK_CUSTOM_MESSAGE,
                                        status.customMessage
                                    )
                                    else -> ClaimFailureState
                                }
                            }
                            else -> ClaimFailureState
                        }
                    } else {
                        ClaimSuccessState
                    }
                }
                is Result.Failure -> {
                    if (isInternetIssue(it.error)) {
                        emitViewEvent(ViewEvent.InternetIssue)
                    } else {
                        emitViewEvent(ViewEvent.ServerError)
                    }
                    ClaimFailureState
                }
            }
        }
    }

    private fun revealReward(): Observable<PartialState> {
        return intent<Reveal>().map {
            if (!reward.isClaimed()) {
                if (reward.isNonCashReward()) {
                    pushIntent(RevealNonCashReward)
                } else {
                    if (reward.amount > 0) {
                        pushIntent(RevealCashReward)
                    } else {
                        pushIntent(ClaimReward)
                    }
                }
            }
            NoChange
        }
    }

    private fun isNonCashReward(): Observable<PartialState> {
        return intent<Load>().map {
            NonCashReward(reward.isNonCashReward())
        }
    }

    private fun revealCashReward(): Observable<PartialState> {
        return intent<RevealCashReward>().switchMap {
            wrap(getCollectionActivationStatus.get().execute())
        }.map {
            when (it) {
                is Result.Success -> {
                    if (it.value) {
                        pushIntent(ClaimReward)
                    } else {
                        emitViewEvent(ViewEvent.Congratulate)
                        emitViewEvent(ViewEvent.ShowAddPaymentDetailsDialog(reward.amount))
                    }
                }
                else -> NoChange
            }
            NoChange
        }
    }

    private fun getMerchantAddress(): Observable<PartialState> {
        return intent<Load>()
            .switchMap {
                getMerchantAddress.get().execute()
            }.map {
                when (it) {
                    is Result.Success -> {
                        MerchantAddress(it.value)
                    }
                    else -> {
                        NoChange
                    }
                }
            }
    }

    private fun revealNonCashReward(): Observable<PartialState> {
        return intent<RevealNonCashReward>().map {
            pushIntent(ClaimReward)
            emitViewEvent(ViewEvent.NonCashRewardWon)
            NoChange
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is NoChange -> currentState
            ClaimSuccessState -> currentState.copy(claimed = true, claimInProgress = false)
            ClaimFailureState -> currentState.copy(claimed = false, claimInProgress = false)
            ClaimProgressState -> currentState.copy(claimInProgress = true)
            is MerchantAddress -> currentState.copy(merchantAddress = partialState.address)
            is InAppChatEnabled -> currentState.copy(inAppChatEnabled = partialState.enabled)
            is NonCashReward -> currentState.copy(nonCashReward = partialState.nonCashReward)
            is ClaimErrorState -> currentState.copy(
                rewardClaimErrorState = partialState.claimErrorState,
                claimed = findRewardsClaimed(partialState.claimErrorState),
                claimInProgress = false,
                customerMessage = partialState.customMessage
            )
            is SetShowScratchView -> currentState.copy(canShowScratchView = partialState.canShow)
        }
    }

    private fun findRewardsClaimed(claimErrorState: RewardClaimErrorState): Boolean {
        return claimErrorState == PROCESSING_PAYOUT_DELAYED ||
            claimErrorState == PROCESSING_PAYOUT_STARTED ||
            claimErrorState == PROCESSING_BUDGET_EXHAUSTED ||
            claimErrorState == PROCESSING_CUSTOM_MESSAGE
    }
}
