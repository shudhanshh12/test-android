package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderContract.*
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase.GetReminderIntentAndUpdateLastReminderSendTime
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase.GetReminderToBeSend
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class SendReminderViewModel @Inject constructor(
    initialState: State,
    private val getReminderToBeSend: Lazy<GetReminderToBeSend>,
    private val getReminderIntent: Lazy<GetReminderIntentAndUpdateLastReminderSendTime>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeLoadReminderIntent(),
            observeGetPaymentReminderIntent(),
        )
    }

    private fun observeGetPaymentReminderIntent() = intent<Intent.GetPaymentReminderIntent>()
        .switchMap { wrap(rxSingle { getReminderIntent.get().execute(it.currentReminderProfile) }) }
        .map {
            if (it is Result.Success) {
                emitViewEvent(ViewEvent.SendReminder(it.value))
            }
            PartialState.NoChange
        }

    private fun observeLoadReminderIntent() = intent<Intent.LoadReminder>()
        .switchMap {
            wrap(
                rxSingle {
                    getReminderToBeSend.get()
                        .execute(
                            getCurrentState().presentSendingReminderIndex,
                            getCurrentState().currentSendingReminder,
                            getCurrentState().remindersTobeSend,
                        )
                }
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    if (it.value.isCompletedSendingReminder) {
                        emitViewEvent(ViewEvent.ShowCongratulationsBanner)
                        emitViewEvent(ViewEvent.TrackSendingReminderCompleted)
                        PartialState.CompletedSendingReminders
                    } else {
                        it.value.currentReminderProfile.also { reminderProfile ->
                            pushIntent(Intent.GetPaymentReminderIntent(reminderProfile))
                        }

                        PartialState.SetUpdatedReminders(
                            currentReminderSentCount = it.value.currentReminderIndex + 1,
                            nextSendingReminderIndex = it.value.currentReminderIndex + 1,
                            currentReminderProfile = it.value.currentReminderProfile,
                        )
                    }
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.ShowError)
                    PartialState.NoChange
                }
                else -> PartialState.NoChange
            }
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetUpdatedReminders -> {
                currentState.copy(
                    currentReminderSentCount = partialState.currentReminderSentCount,
                    presentSendingReminderIndex = partialState.nextSendingReminderIndex,
                    leftReminderTobeSendForAnalytics = currentState.leftReminderTobeSendForAnalytics
                        .toMutableList()
                        .apply {
                            remove(partialState.currentReminderProfile)
                        },
                    currentSendingReminder = partialState.currentReminderProfile,
                )
            }
            is PartialState.CompletedSendingReminders -> currentState.copy(
                completedSendingReminders = true
            )
        }
    }
}
