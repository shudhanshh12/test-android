package `in`.okcredit.voice_first.ui.bulk_add

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.ui.bulk_add.drafts_list.DraftsListItem
import androidx.annotation.StringRes
import java.util.*

interface BulkAddTransactionsContract {
    data class State(
        val entries: List<DraftsListItem> = emptyList(),
        val selectedCalendar: Calendar? = null,
        val selectedCalendarString: String? = null,

        val processing: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class Progress(val shown: Boolean) : PartialState()
        data class UpdateEntries(val drafts: List<DraftsListItem>) : PartialState()
        data class UpdateCalendar(val selectedCalendar: Calendar) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object Cancel : Intent()

        data class OnDateSet(val calendar: Calendar) : Intent()
        data class VoiceInputState(val isVoiceError: Boolean) : Intent()
        data class VoiceTranscriptReady(val voiceTranscript: String) : Intent()
        data class RequestDraftTransactionParse(val draft: DraftTransaction) : Intent()

        data class Save(val checkIfPasswordRequired: Boolean) : Intent()
        object CheckFourPinPasswordSet : Intent()
        object SyncMerchantPref : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object EndActivity : ViewEvent()
        object GoToEnterPassword : ViewEvent()
        object ShowUpdatePinDialog : ViewEvent()

        data class ShowMessage(@StringRes val message: Int) : ViewEvent()
    }
}
