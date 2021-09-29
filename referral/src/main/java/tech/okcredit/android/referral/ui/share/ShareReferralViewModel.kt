package tech.okcredit.android.referral.ui.share

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import tech.okcredit.android.referral.ui.share.ShareReferralContract.*
import tech.okcredit.android.referral.usecase.GetReferralIntent
import tech.okcredit.android.referral.usecase.ShareReferralUseCase
import javax.inject.Inject

class ShareReferralViewModel @Inject constructor(
    initialState: State,
    private val getReferralIntent: Lazy<GetReferralIntent>,
    private val shareReferralUseCase: Lazy<ShareReferralUseCase>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    override fun handle(): Observable<UiState.Partial<State>> {
        return mergeArray(
            whatsAppShareReferral(),
            loadIntent()
        )
    }

    private fun loadIntent(): Observable<PartialState> {
        return intent<Intent.Load>()
            .switchMap {
                wrap(shareReferralUseCase.get().shouldShowShareNudge())
            }.map {
                when (it) {
                    is Result.Success ->
                        if (it.value) {
                            PartialState.ShowNudge
                        } else {
                            PartialState.HideNudge
                        }
                    else -> PartialState.HideNudge
                }
            }
    }

    private fun whatsAppShareReferral(): Observable<PartialState> {
        return intent<Intent.WhatsAppShare>()
            .switchMap { getReferralIntent.get().getWhatsAppIntent() }
            .flatMap {
                when (it) {
                    is Result.Progress -> Observable.just(PartialState.ShowProgress)
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.ShareToWhatsApp(it.value))
                        shareReferralUseCase.get().setShareNudge(false)
                            .andThen(Observable.just(PartialState.HideProgress))
                    }
                    is Result.Failure -> {
                        emitViewEvent(ViewEvent.ShareFailure)
                        Observable.just(PartialState.HideProgress)
                    }
                }
            }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            PartialState.ShowProgress -> currentState.copy(showProgress = true)
            PartialState.HideProgress -> currentState.copy(showProgress = false)
            PartialState.ShowNudge -> currentState.copy(showNudge = true)
            PartialState.HideNudge -> currentState.copy(showNudge = false)
        }
    }
}
