package `in`.okcredit.voice_first.ui.voice_collection

import `in`.okcredit.fileupload.usecase.IUploadAudioSampleFile
import `in`.okcredit.fileupload.usecase.IUploadAudioSampleFile.Companion.AWS_AUDIO_SAMPLES_BASE_URL
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Property
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Property.DURATION
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Property.INPUT_TEXT
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Property.SCREEN
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Property.SOURCE
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Value
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Value.BOOSTER_VOICE_COLLECTION_SCREEN
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Value.MAXIMUM_TIME
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Value.MINIMUM_TIME
import `in`.okcredit.voice_first.analytics.BoosterVoiceCollectionTracker.Value.SOURCE_BOOSTER_QUESTION
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionContract.*
import `in`.okcredit.voice_first.usecase.GetVoiceBoosterText
import `in`.okcredit.voice_first.usecase.SubmitVoiceBoosterText
import `in`.okcredit.voice_first.usecase.VoiceRecorder
import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.deleteIfExists
import tech.okcredit.android.base.extensions.makeIfNotExists
import tech.okcredit.base.permission.Permission
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BoosterVoiceCollectionViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val voiceRecorder: Lazy<VoiceRecorder>,
    private val context: Lazy<Context>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val uploadAudioSampleFile: Lazy<IUploadAudioSampleFile>,
    private val tracker: Lazy<BoosterVoiceCollectionTracker>,
    private val getVoiceBoosterText: Lazy<GetVoiceBoosterText>,
    private val submitVoiceBoosterText: Lazy<SubmitVoiceBoosterText>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    companion object {
        const val FRC_KEY_OKPL_AUDIO_SAMPLE_MINIMUM_DURATION_MILLIS = "okpl_audio_sample_minimum_duration_millis"
        const val FRC_KEY_OKPL_AUDIO_SAMPLE_MAXIMUM_DURATION_MILLIS = "okpl_audio_sample_maximum_duration_millis"
        const val FILE_EXTENSION = ".wav"
        const val FOLDER_NAME = "okpl"
    }

    private val folderPath by lazy {
        "${context.get().cacheDir}/$FOLDER_NAME"
    }

    lateinit var filePath: String

    private var isDeepLinkFlow = false

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            observeLoad(),
            observeGreetingEducationCompleted(),
            observeStartRecording(),
            observeStopRecording(),
            observeCancelRecording(),
            observeRetryRecording(),
            observeTaskCompleted(),
            loadVoiceBoosterText(),
            recordSubmitIfDeepLinkFlow(),
        )
    }

    private fun loadVoiceBoosterText() = intent<Intent.Load>()
        .filter { getCurrentState().voiceMessageInput.isBlank() }
        .switchMap {
            isDeepLinkFlow = true
            wrap {
                getVoiceBoosterText.get().execute()
            }
        }.map {
            when (it) {
                is Result.Progress -> PartialState.LoadingVoiceBoosterText
                is Result.Success -> {
                    emitViewEvent(ViewEvent.PlayGreetingEducation)
                    PartialState.SetVoiceBoosterText(it.value)
                }
                is Result.Failure -> {
                    PartialState.VoiceBoosterTextFailed
                }
            }
        }

    private fun recordSubmitIfDeepLinkFlow() = intent<Intent.RecordSubmit>()
        .switchMap {
            wrap {
                submitVoiceBoosterText.get().execute()
            }
        }.map {
            if (it is Result.Progress) {
                PartialState.NoChange
            }
            pushIntent(Intent.TaskCompleted)
            PartialState.NoChange
        }

    private fun getRandomFileName() = "$folderPath/${UUID.randomUUID()}$FILE_EXTENSION"

    private fun observeLoad() = intent<Intent.Load>()
        .filter { getCurrentState().voiceMessageInput.isNotBlank() }
        .map {
            emitViewEvent(ViewEvent.PlayGreetingEducation)
            PartialState.NoChange
        }

    private fun observeGreetingEducationCompleted() = intent<Intent.GreetingEducationCompleted>()
        .map {
            PartialState.RecorderIdle
        }

    private fun observeStartRecording() = intent<Intent.StartRecording>()
        .map {
            val isPermissionGranted = Permission.isRecordAudioPermissionAlreadyGranted(context.get())
            if (isPermissionGranted) {
                makeFolderIfNotPresent(folderPath)
                filePath = getRandomFileName()
                voiceRecorder.get().startRecordingIntoFile(filePath)
                tracker.get().recordingStarted()
                PartialState.StartRecording
            } else {
                emitViewEvent(ViewEvent.RequestAudioPermission)
                PartialState.NoChange
            }
        }

    private fun makeFolderIfNotPresent(path: String) = File(path).makeIfNotExists()

    private fun deleteAudioRecordingIfPresent() = File(filePath).deleteIfExists()

    private fun observeStopRecording() = intent<Intent.StopRecording>()
        .switchMap {
            voiceRecorder.get().stopRecording()
            if (isRecordingValid(it.duration)) {
                tracker.get().submitButtonClicked(true, it.duration)
                val remoteUrl = "$AWS_AUDIO_SAMPLES_BASE_URL/${UUID.randomUUID()}$FILE_EXTENSION"
                uploadAudioSampleFile.get().schedule(remoteUrl, filePath, buildTrackerPropertiesForUpload(it))
                    .doOnComplete { doIfDeepLinkFlow() }
                    .andThen(Observable.just(PartialState.ValidRecording))
            } else {
                trackSubmitButtonClicked(it)
                deleteAudioRecordingIfPresent()
                Observable.just(PartialState.InvalidRecording)
            }
        }

    private fun doIfDeepLinkFlow() {
        if (isDeepLinkFlow) {
            pushIntent(Intent.RecordSubmit)
        } else {
            pushIntent(Intent.TaskCompleted)
        }
    }

    private fun trackSubmitButtonClicked(intent: Intent.StopRecording) {
        if (intent.duration == -1) {
            val duration = firebaseRemoteConfig.get().getLong(FRC_KEY_OKPL_AUDIO_SAMPLE_MAXIMUM_DURATION_MILLIS).toInt()
            tracker.get().submitButtonClicked(false, duration, MAXIMUM_TIME)
        } else {
            tracker.get().submitButtonClicked(false, intent.duration, MINIMUM_TIME)
        }
    }

    private fun buildTrackerPropertiesForUpload(intent: Intent.StopRecording) = mapOf(
        SOURCE to SOURCE_BOOSTER_QUESTION,
        INPUT_TEXT to intent.inputText,
        DURATION to intent.duration.toString(),
        SCREEN to BOOSTER_VOICE_COLLECTION_SCREEN,
        Property.CAMPAIGN to Value.CAMPAIGN
    )

    private fun isRecordingValid(duration: Int) =
        duration >= firebaseRemoteConfig.get().getLong(FRC_KEY_OKPL_AUDIO_SAMPLE_MINIMUM_DURATION_MILLIS)

    private fun observeCancelRecording() = intent<Intent.CancelRecording>()
        .map {
            voiceRecorder.get().stopRecording()
            deleteAudioRecordingIfPresent()
            PartialState.RecorderIdle
        }

    private fun observeTaskCompleted() = intent<Intent.TaskCompleted>()
        .delay(2, TimeUnit.SECONDS)
        .map {
            emitViewEvent(ViewEvent.TaskCompleted)
            PartialState.NoChange
        }

    private fun observeRetryRecording() = intent<Intent.RetryRecording>()
        .map {
            PartialState.RecorderIdle
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.StartRecording -> currentState.copy(recorderState = RecorderState.RECORDING)
            is PartialState.InvalidRecording -> currentState.copy(recorderState = RecorderState.INVALID)
            is PartialState.RecorderIdle -> currentState.copy(recorderState = RecorderState.IDLE)
            is PartialState.ValidRecording -> currentState.copy(recorderState = RecorderState.COMPLETED)
            is PartialState.SetVoiceBoosterText -> currentState.copy(
                voiceMessageInput = partialState.voiceMessageInput,
                voiceBoosterLoading = false,
                voiceBoosterError = false,
                isDeepLinkFlow = true
            )
            PartialState.LoadingVoiceBoosterText -> currentState.copy(
                voiceBoosterLoading = true,
                voiceBoosterError = false,
                isDeepLinkFlow = true
            )
            PartialState.VoiceBoosterTextFailed -> currentState.copy(
                voiceBoosterError = true,
                voiceBoosterLoading = false
            )
        }
    }
}
