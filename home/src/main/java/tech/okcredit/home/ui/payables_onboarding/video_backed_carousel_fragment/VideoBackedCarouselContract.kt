package tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface VideoBackedCarouselContract {

    object State : UiState

    object PartialState : UiState.Partial<State>

    object Intent : UserIntent

    object ViewEvent : BaseViewEvent
}
