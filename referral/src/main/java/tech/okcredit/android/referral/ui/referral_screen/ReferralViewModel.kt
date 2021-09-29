package tech.okcredit.android.referral.ui.referral_screen

import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.shared.utils.ScreenName
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.ui.referral_screen.ReferralContract.*
import tech.okcredit.android.referral.usecase.SyncReferral
import tech.okcredit.android.referral.utils.GetReferralVersionImpl
import tech.okcredit.feature_help.contract.GetContextualHelpIds
import javax.inject.Inject

class ReferralViewModel @Inject constructor(
    initialState: State,
    private val syncReferral: Lazy<SyncReferral>,
    private val getReferralVersionImpl: Lazy<GetReferralVersionImpl>,
    private val referralEventTracker: Lazy<ReferralEventTracker>,
    private val getContextualHelpIds: Lazy<GetContextualHelpIds>,
) : BaseViewModel<State, PartialState, ReferralViewEvent>(initialState) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            observeContextualHelpIdsOnLoad(),
            loadReferralVersion(),
            syncReferralContent()
        )
    }

    private fun observeContextualHelpIdsOnLoad() = intent<Intent.Load>().switchMap {
        wrap(getContextualHelpIds.get().execute(ScreenName.ShareOkCreditScreen.value))
    }.map {
        if (it is Result.Success) {
            return@map PartialState.SetContextualHelpIds(it.value)
        }
        PartialState.NoChange
    }

    private fun syncReferralContent() =
        UseCase.wrapCompletable(syncReferral.get().execute()).map { PartialState.NoChange }

    private fun loadReferralVersion(): Observable<PartialState>? {
        return UseCase.wrapObservable(getReferralVersionImpl.get().execute())
            .map {
                if (it is Result.Success) {
                    referralEventTracker.get().trackReferralScreenViewed(it.value)
                    when (it.value) {
                        ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION,
                        ReferralVersion.TARGETED_REFERRAL,
                        -> {
                            emitViewEvent(ReferralViewEvent.ShowTargetedUsersFragment)
                        }
                        ReferralVersion.REWARDS_ON_ACTIVATION -> {
                            emitViewEvent(ReferralViewEvent.ShowShareFragment)
                        }
                        ReferralVersion.UNKNOWN,
                        ReferralVersion.NO_REWARD,
                        -> {
                            emitViewEvent(ReferralViewEvent.NoReferralError)
                        }
                    }
                    PartialState.SetReferralVersion(it.value)
                } else {
                    PartialState.NoChange
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetReferralVersion -> currentState.copy(version = partialState.version)
            is PartialState.SetContextualHelpIds -> currentState.copy(contextualHelpIds = partialState.helpIds)
        }
    }
}
