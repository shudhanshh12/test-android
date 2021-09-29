package `in`.okcredit.merchant.ui.create_business

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import java.lang.ref.WeakReference

interface CreateBusinessContract {

    data class State(
        val loading: Boolean = false,
        val successful: Boolean = false,
    ) : UiState

    sealed class Intent : UserIntent {
        data class CreateBusiness(val businessName: String, val weakActivity: WeakReference<Activity>) : Intent()
        object AutoDismissAndGoToHome : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetLoading(val loading: Boolean) : PartialState()
        object SetSuccessful : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(val msg: String) : ViewEvent()
        object CreateSuccessful : ViewEvent()
        object DismissAndGoHome : ViewEvent()
    }
}
