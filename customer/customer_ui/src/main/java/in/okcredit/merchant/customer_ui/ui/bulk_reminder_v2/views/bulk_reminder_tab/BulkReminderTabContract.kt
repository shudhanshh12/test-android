package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BulkReminderTabContract {
    object State : UiState

    sealed class PartialState : UiState.Partial<State>

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
