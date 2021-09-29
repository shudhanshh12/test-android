package tech.okcredit.android.referral.share

import `in`.okcredit.referral.contract.usecase.GetShareAppIntent
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.android.referral.share.ShareAppContract.Intent
import tech.okcredit.android.referral.share.ShareAppContract.PartialState
import tech.okcredit.android.referral.share.ShareAppContract.State
import tech.okcredit.android.referral.share.ShareAppContract.ViewEvent
import tech.okcredit.android.referral.share.usecase.GetReferralDescriptionVisibility
import javax.inject.Inject

class ShareAppViewModel @Inject constructor(
    state: State,
    private val getShareAppIntent: Lazy<GetShareAppIntent>,
    private val getReferralDescriptionVisibility: Lazy<GetReferralDescriptionVisibility>,
) : BaseViewModel<State, PartialState, ViewEvent>(state) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            shareOnWhatsApp(),
            showReferralDescription()
        )
    }

    private fun showReferralDescription() = intent<Intent.Load>()
        .switchMap { wrap(getReferralDescriptionVisibility.get().execute()) }
        .map {
            when (it) {
                is Result.Success -> PartialState.CanShowReferralDescription(it.value)
                else -> PartialState.NoChange
            }
        }

    private fun shareOnWhatsApp(): Observable<PartialState> {
        return intent<Intent.ShareOnWhatsApp>()
            .switchMap { UseCase.wrapSingle(getShareAppIntent.get().execute()) }
            .map {
                when (it) {
                    is Result.Progress -> PartialState.ShowProgressBar
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.ShareApp(it.value))
                        PartialState.HideProgressBar
                    }
                    is Result.Failure -> {
                        emitViewEvent(ViewEvent.ShareAppFailure)
                        PartialState.HideProgressBar
                    }
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            PartialState.ShowProgressBar -> currentState.copy(showProgressBar = true)
            PartialState.HideProgressBar -> currentState.copy(showProgressBar = false)
            is PartialState.CanShowReferralDescription -> currentState.copy(
                canShowReferralDescription = partialState.canShow
            )
        }
    }
}
