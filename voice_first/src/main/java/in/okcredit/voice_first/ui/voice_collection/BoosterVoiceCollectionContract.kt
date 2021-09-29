package `in`.okcredit.voice_first.ui.voice_collection

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BoosterVoiceCollectionContract {
    data class State(
        val voiceMessageInput: String,
        val recorderState: RecorderState = RecorderState.GREETING,
        val isDeepLinkFlow: Boolean = false,
        val voiceBoosterLoading: Boolean = false,
        val voiceBoosterError: Boolean = false,
    ) : UiState

    enum class RecorderState { GREETING, IDLE, RECORDING, COMPLETED, INVALID }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object StartRecording : PartialState()
        object InvalidRecording : PartialState()
        object ValidRecording : PartialState()
        object RecorderIdle : PartialState()
        data class SetVoiceBoosterText(val voiceMessageInput: String) : PartialState()
        object LoadingVoiceBoosterText : PartialState()
        object VoiceBoosterTextFailed : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object GreetingEducationCompleted : Intent()
        object StartRecording : Intent()
        object CancelRecording : Intent()
        data class StopRecording(val duration: Int, val inputText: String) : Intent()
        object RetryRecording : Intent()
        object TaskCompleted : Intent()
        object RecordSubmit : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object TaskCompleted : ViewEvent()
        object RequestAudioPermission : ViewEvent()
        object PlayGreetingEducation : ViewEvent()
    }
}
