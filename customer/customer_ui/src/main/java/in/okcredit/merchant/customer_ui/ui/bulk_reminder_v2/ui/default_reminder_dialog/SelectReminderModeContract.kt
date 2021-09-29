package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderMode
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UiState.Partial
import `in`.okcredit.shared.base.UserIntent

interface SelectReminderModeContract {
    data class State(
        val customerId: String? = null,
        val reminderMode: ReminderMode? = null,
    ) : UiState

    sealed class PartialState : Partial<State> {
        object NoChange : PartialState()
        data class SetReminderMode(val mode: ReminderMode) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class SetReminderMode(val mode: ReminderMode) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class SetResult(val customerId: String?, val mode: ReminderMode) : ViewEvent()
    }
}
