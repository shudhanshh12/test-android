package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeContract.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import io.reactivex.Observable
import javax.inject.Inject

class SelectReminderModeViewModel @Inject constructor(
    initialState: State,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeSetReminderModeIntent()
        )
    }

    private fun observeSetReminderModeIntent() = intent<Intent.SetReminderMode>()
        .map {
            PartialState.SetReminderMode(it.mode)
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetReminderMode -> {
                emitViewEvent(ViewEvent.SetResult(currentState.customerId, partialState.mode))
                currentState.copy(
                    reminderMode = partialState.mode
                )
            }
        }
    }
}
