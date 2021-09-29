package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.ReferralInAppContract.*
import tech.okcredit.android.referral.ui.referral_in_app_bottomsheet.usecase.SetReferralInAppShown
import javax.inject.Inject

class ReferralInAppViewModel @Inject constructor(
    private val setReferralInAppShown: Lazy<SetReferralInAppShown>
) : BaseViewModel<State, PartialState, ViewEvent>(State) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return setReferralInAppShown()
    }

    private fun setReferralInAppShown() = intent<Intent.Load>()
        .switchMap { wrap(setReferralInAppShown.get().execute()) }
        .map {
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
        }
    }
}
