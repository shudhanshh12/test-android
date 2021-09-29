package `in`.okcredit.voice_first.usecase

import `in`.okcredit.voice_first.R
import android.content.Context
import android.media.MediaPlayer
import dagger.Lazy
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class EducationAudioPlayerForVoiceCollectionBooster @Inject constructor(
    private val context: Lazy<Context>,
) {

    private var mediaPlayer: MediaPlayer? = null

    fun playGreetingEducation(onCompleteListener: () -> Unit) {
        try {
            mediaPlayer = MediaPlayer.create(context.get(), R.raw.okpl_voice_collection).apply {
                start()
                setOnCompletionListener {
                    it.release()
                    onCompleteListener.invoke()
                }
            }
        } catch (e: Exception) {
            RecordException.recordException(e)
            onCompleteListener.invoke()
        }
    }

    fun stopPlayGreetingEducation() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }
}
