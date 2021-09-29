package `in`.okcredit.voice_first.usecase

import android.media.MediaRecorder
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Reusable
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

// TODO : move this class to voice related module after it's created.
//  Doesn't make sense to create a new module just for one file.
@Reusable
class VoiceRecorder @Inject constructor(
    firebaseRemoteConfig: FirebaseRemoteConfig,
) {

    companion object {
        private const val ENCODING_BIT_RATE = 48000
        const val FRC_KEY_VOICE_COLLECTION_SAMPLE_RATE = "voice_collection_sample_rate"
    }

    private val sampleRate = firebaseRemoteConfig.getLong(FRC_KEY_VOICE_COLLECTION_SAMPLE_RATE).toInt()
    private var recorder: MediaRecorder? = null

    fun startRecordingIntoFile(fileName: String) {
        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(sampleRate)
                setAudioEncodingBitRate(ENCODING_BIT_RATE)
                setOutputFile(fileName)
                prepare()
                start()
            }
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }
}
