package tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselContract.*
import javax.inject.Inject

class VideoBackedCarouselViewModel @Inject constructor() : BaseViewModel<State, PartialState, ViewEvent>(
    State
) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.empty()
    }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return currentState
    }
}
