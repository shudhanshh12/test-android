package tech.okcredit.android.referral.ui.know_more

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.android.base.extensions.isGreaterThanZero
import tech.okcredit.android.referral.ui.know_more.ReferralKnowMoreContract.*
import tech.okcredit.android.referral.usecase.GetTotalRewardAmountFromReferral
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import javax.inject.Inject

class ReferralKnowMoreViewModel @Inject constructor(
    initialState: State,
    private val referralRepository: Lazy<ReferralRepository>,
    private val getReferralVersionImpl: Lazy<GetReferralVersionImpl>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getTotalRewardAmountFromReferral: Lazy<GetTotalRewardAmountFromReferral>
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private var unclaimedRewards: Long? = null

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            loadReferralVersion(),
            getReferralInfo(),
            getTotalRewards(),
            showRewards()
        )
    }

    private fun loadReferralVersion(): Observable<PartialState>? {
        return UseCase.wrapObservable(getReferralVersionImpl.get().execute())
            .map {
                if (it is Result.Success) {
                    PartialState.SetReferralVersion(it.value)
                } else {
                    PartialState.NoChange
                }
            }
    }

    private fun getReferralInfo(): Observable<PartialState> {
        return UseCase.wrapObservable(referralRepository.get().getReferralInfo())
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> PartialState.SetReferralInfo(it.value)
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun getTotalRewards(): Observable<PartialState> {
        return getTotalRewardAmountFromReferral.get().execute(Unit)
            .map {
                when (it) {
                    is Result.Progress -> PartialState.NoChange
                    is Result.Success -> {
                        unclaimedRewards = it.value.totalUnClaimedReferralReward
                        PartialState.SetTotalRewards(
                            it.value.totalClaimedReferralReward,
                            it.value.totalUnClaimedReferralReward
                        )
                    }
                    is Result.Failure -> PartialState.NoChange
                }
            }
    }

    private fun showRewards(): Observable<PartialState> {
        return intent<Intent.GoToReferralRewards>()
            .switchMap { UseCase.wrapObservable(collectionRepository.get().isCollectionActivated()) }
            .map {
                when (it) {
                    is Result.Success -> {
                        if (!it.value && unclaimedRewards.isGreaterThanZero()) {
                            emitViewEvent(ViewEvent.ShowCollectionDialog(unclaimedRewards!!))
                        } else {
                            emitViewEvent(ViewEvent.GoToReferralRewards)
                        }
                    }
                }
                PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetReferralInfo -> currentState.copy(referralInfo = partialState.referralInfo)
            is PartialState.SetTotalRewards -> currentState.copy(
                totalClaimedReferralRewards = partialState.totalClaimedReferralRewards,
                totalUnclaimedReferralRewards = partialState.totalUnclaimedReferralRewards
            )
            is PartialState.SetReferralVersion -> currentState.copy(version = partialState.version)
        }
    }
}
