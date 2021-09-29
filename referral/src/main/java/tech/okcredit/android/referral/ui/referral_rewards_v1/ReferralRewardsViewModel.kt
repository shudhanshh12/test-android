package tech.okcredit.android.referral.ui.referral_rewards_v1

import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.data.GetReferredMerchantsResponse
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.PartialState
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.PartialState.*
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.State
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.ViewEvent
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.ViewEvent.DeferredScrollToPos
import tech.okcredit.android.referral.ui.ReferralActivity
import tech.okcredit.android.referral.ui.referral_rewards_v1.usecase.GetReferredMerchants
import tech.okcredit.android.referral.ui.referral_rewards_v1.usecase.NotifyMerchant
import tech.okcredit.android.referral.usecase.GetTotalRewardAmountFromReferral
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class ReferralRewardsViewModel @Inject constructor(
    initialState: State,
    private val getRewards: Lazy<GetTotalRewardAmountFromReferral>,
    private val getReferredMerchants: Lazy<GetReferredMerchants>,
    private val notifyMerchant: Lazy<NotifyMerchant>,
    private val getReferralVersionImpl: Lazy<GetReferralVersionImpl>,
    private val eventTracker: Lazy<ReferralEventTracker>,
    @ViewModelParam(ReferralActivity.ARG_TARGETED_REFERRAL_PHONE_NUMBER)
    private val scrollToPhoneNumberOnce: Lazy<String?>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var referralVersion = ReferralVersion.REWARDS_ON_ACTIVATION

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadIntent(),
            setReferralVersion(),
            hideMessageIntent(),
            notifyIntent()
        )
    }

    private fun loadIntent(): Observable<PartialState>? {
        return intent<ReferralRewardsContract.Intent.Load>()
            .switchMap {
                Observable.zip(
                    getReferredMerchants.get().execute(),
                    getRewards.get().execute(Unit),
                    getPartialStateForLoadIntent()
                )
            }.doAfterNext {
                if (it is Error) {
                    pushIntent(ReferralRewardsContract.Intent.HideToastMessage)
                }
            }
    }

    private fun setReferralVersion(): Observable<PartialState>? {
        return intent<ReferralRewardsContract.Intent.Load>()
            .switchMap { getReferralVersionImpl.get().execute() }
            .map {
                referralVersion = it
                eventTracker.get().trackReferralRewardsViewed(it)
                setReferralVersion(referralVersion = it)
            }
    }

    private fun getPartialStateForLoadIntent():
        BiFunction<Result<GetReferredMerchantsResponse>,
            Result<GetTotalRewardAmountFromReferral.Response>,
            PartialState> {
        return BiFunction { referredMerchantsResult, rewardsResult ->
            return@BiFunction if (referredMerchantsResult is Result.Success && rewardsResult is Result.Success) {
                val rewards = rewardsResult.value
                val earnings = formatV2(rewards.totalClaimedReferralReward)
                val unclaimedRewards = if (rewards.totalUnClaimedReferralReward > 0) {
                    formatV2(rewards.totalUnClaimedReferralReward)
                } else {
                    null
                }

                scrollToPhoneNumberOnce.get()?.also { phoneNumber ->
                    val pos = referredMerchantsResult.value.referredMerchants
                        .indexOfFirst { it.phoneNumber == phoneNumber }
                    emitViewEvent(DeferredScrollToPos(pos))
                }

                LoadCompleted(earnings, unclaimedRewards, referredMerchantsResult.value.referredMerchants)
            } else if (rewardsResult is Result.Progress || referredMerchantsResult is Result.Progress) {
                ShowProgress
            } else {
                Error(R.string.referred_merchant_list_error)
            }
        }
    }

    private fun hideMessageIntent(): Observable<PartialState>? =
        intent<ReferralRewardsContract.Intent.HideToastMessage>().map { HideMessage }

    private fun notifyIntent(): Observable<PartialState>? {
        return intent<ReferralRewardsContract.Intent.Notify>()
            .switchMap { notifyMerchant.get().execute(it.phoneNumber) }
            .map {
                when (it) {
                    is Result.Progress -> ShowProgress
                    is Result.Success -> {
                        if (it.value.error == "already_notified") {
                            ShowMessage(R.string.merchant_already_notified)
                        } else {
                            ShowMessage(R.string.notify_merchant_success)
                        }
                    }
                    is Result.Failure -> ShowMessage(R.string.notify_merchant_error)
                }
            }.doAfterNext {
                if (it is ShowMessage) {
                    pushIntent(ReferralRewardsContract.Intent.HideToastMessage)
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is ShowProgress -> currentState.copy(showProgressBar = true)
            is ShowMessage -> currentState.copy(
                showProgressBar = false,
                toastMessage = partialState.message
            )
            is HideMessage -> currentState.copy(toastMessage = null)
            is LoadCompleted -> currentState.copy(
                showProgressBar = false,
                earnings = partialState.earnings,
                unclaimedRewards = partialState.unclaimedRewards,
                referredMerchants = partialState.referredMerchants,
                errorMessage = null,
                showError = false
            )
            is MerchantNotified -> currentState.copy(
                showProgressBar = false,
                toastMessage = R.string.merchant_notified
            )
            is Error -> currentState.copy(
                showProgressBar = false,
                toastMessage = partialState.message,
                errorMessage = partialState.message,
                showError = true
            )
            is NoChange -> currentState
            is setReferralVersion -> currentState.copy(
                referralVersion = partialState.referralVersion
            )
        }
    }
}
