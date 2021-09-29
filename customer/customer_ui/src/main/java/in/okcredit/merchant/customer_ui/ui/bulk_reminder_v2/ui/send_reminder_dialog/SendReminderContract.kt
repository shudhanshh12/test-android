package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SendReminderContract {

    data class State(
        val remindersTobeSend: List<ReminderProfile> = emptyList(),
        val allReminderListedForAnalytics: List<ReminderProfile> = emptyList(),
        val leftReminderTobeSendForAnalytics: List<ReminderProfile> = emptyList(),
        val totalRemindersCountToBeDispatch: Int = 0,
        val presentSendingReminderIndex: Int = 0,
        val currentSendingReminder: ReminderProfile? = null,
        val sendingReminderIntent: android.content.Intent? = null,
        val completedSendingReminders: Boolean = false,
        val currentReminderSentCount: Int = 0,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetUpdatedReminders(
            val currentReminderSentCount: Int,
            val nextSendingReminderIndex: Int,
            val currentReminderProfile: ReminderProfile?,
        ) : PartialState()

        object CompletedSendingReminders : PartialState()

        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object LoadReminder : Intent()

        data class GetPaymentReminderIntent(val currentReminderProfile: ReminderProfile) : Intent()

        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowCongratulationsBanner : ViewEvent()
        object ShowError : ViewEvent()
        object TrackSendingReminderCompleted : ViewEvent()

        data class SendReminder(
            val intent: android.content.Intent,
        ) : ViewEvent()
    }
}
