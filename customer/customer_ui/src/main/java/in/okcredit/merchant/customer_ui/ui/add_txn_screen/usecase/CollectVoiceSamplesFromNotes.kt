package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import `in`.okcredit.fileupload.usecase.IUploadAudioSampleFile
import `in`.okcredit.home.HomePreferences
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import android.net.Uri
import android.speech.RecognizerIntent
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.Scope
import java.util.*
import javax.inject.Inject

class CollectVoiceSamplesFromNotes @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
    private val preference: Lazy<HomePreferences>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private var uploadAudioSampleFile: Lazy<IUploadAudioSampleFile>,
) {

    companion object {
        @NonNls
        private const val FEATURE_COLLECT_VOICE_SAMPLES_FROM_NOTES = "collect_voice_samples_from_notes"

        @NonNls
        private const val VOICE_SAMPLES_FROM_NOTES_COLLECTED_COUNT = "voice_samples_from_notes_collected_count"

        @NonNls
        private const val REMOTE_CONFIG_VOICE_SAMPLES_FROM_NOTES_MAX_COUNT = "voice_samples_from_notes_max_count"

        @NonNls
        private const val RECOGNIZER_INTENT_GET_AUDIO_FORMAT = "android.speech.extra.GET_AUDIO_FORMAT"

        @NonNls
        private const val RECOGNIZER_INTENT_AUDIO_FORMAT = "audio/AMR"

        @NonNls
        private const val RECOGNIZER_INTENT_GET_AUDIO = "android.speech.extra.GET_AUDIO"

        private const val DURATION = 4000

        private const val OPT_OUT_COUNT = 100
    }

    fun shouldCollectVoiceSamplesFromNotes(): Observable<Boolean> {
        return abRepository.get().isFeatureEnabled(FEATURE_COLLECT_VOICE_SAMPLES_FROM_NOTES)
            .flatMap { enabled ->
                if (enabled) {
                    val maxCount = firebaseRemoteConfig.get().getLong(REMOTE_CONFIG_VOICE_SAMPLES_FROM_NOTES_MAX_COUNT)
                    preference.get().getInt(VOICE_SAMPLES_FROM_NOTES_COLLECTED_COUNT, Scope.Individual)
                        .asObservable()
                        .map { it < maxCount }
                } else {
                    Observable.just(false)
                }
            }
    }

    fun incrementVoiceSampleCollectedCount(): Completable =
        rxCompletable { preference.get().increment(VOICE_SAMPLES_FROM_NOTES_COLLECTED_COUNT, Scope.Individual) }

    fun getSpeechRecognitionIntent(): android.content.Intent {
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val language = Locale.getDefault()
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, DURATION)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, DURATION)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, DURATION)
        intent.putExtra(RECOGNIZER_INTENT_GET_AUDIO_FORMAT, RECOGNIZER_INTENT_AUDIO_FORMAT)
        intent.putExtra(RECOGNIZER_INTENT_GET_AUDIO, true)
        return intent
    }

    fun optOut() = rxCompletable {
        preference.get().set(VOICE_SAMPLES_FROM_NOTES_COLLECTED_COUNT, OPT_OUT_COUNT, Scope.Individual)
    }

    fun scheduleUpload(uri: Uri, transcribedText: String, noteText: String, transactionId: String): Completable {
        val remoteUrl = "${IUploadAudioSampleFile.AWS_AUDIO_SAMPLES_BASE_URL}/${UUID.randomUUID()}.mp3"

        return uploadAudioSampleFile.get().createLocalFile(uri, remoteUrl)
            .flatMapCompletable { path ->
                preference.get().getInt(VOICE_SAMPLES_FROM_NOTES_COLLECTED_COUNT, Scope.Individual)
                    .asObservable()
                    .firstOrError()
                    .flatMapCompletable { count ->
                        val trackerProperties = mapOf(
                            CustomerEventTracker.SOURCE to CustomerEventTracker.VOICE_NOTES,
                            CustomerEventTracker.TRANSCRIBED_TEXT to transcribedText,
                            CustomerEventTracker.NOTE_TEXT to noteText,
                            CustomerEventTracker.TRANSACTION_ID to transactionId,
                            CustomerEventTracker.SAMPLE_COUNT to (count + 1).toString(),
                        )
                        uploadAudioSampleFile.get().schedule(remoteUrl, path, trackerProperties)
                    }
            }
            .andThen(incrementVoiceSampleCollectedCount())
    }
}
