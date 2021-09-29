package `in`.okcredit.voice_first.ui.bulk_add.voice_parse

import `in`.okcredit.voice_first.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.UiThread
import timber.log.Timber
import java.util.*

class SpeechInput constructor(
    private val speechRecognizer: SpeechRecognizer,
    private val applicationContext: Context,
    private val onSpeechInputListener: OnSpeechInputListener
) {

    var isUserSpeaking = false
    private var intent: Intent

    interface OnSpeechInputListener {

        fun showAlertMessage(alertMessage: String)

        fun onTextResult(result: String)

        fun startVoiceIconAnimation()

        fun stopVoiceIconAnimation()
    }

    init {
        recognitionListener()

        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val language = Locale.getDefault()
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, applicationContext.packageName)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        val duration = 4000
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, duration)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, duration)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, duration)
    }

    @UiThread
    fun startListening() {
        try {
            isUserSpeaking = true
            speechRecognizer.startListening(intent)
            onSpeechInputListener.startVoiceIconAnimation()
        } catch (e: SecurityException) {
            throw GoogleVoiceTypingDisabledException()
        }
    }

    @UiThread
    fun stopListening() {
        isUserSpeaking = false
        speechRecognizer.stopListening()
        onSpeechInputListener.stopVoiceIconAnimation()
    }

    @UiThread
    private fun recognitionListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                //
            }

            override fun onRmsChanged(p0: Float) {
                //
            }

            override fun onBufferReceived(p0: ByteArray?) {
                //
            }

            override fun onPartialResults(p0: Bundle?) {
                //
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                //
            }

            override fun onBeginningOfSpeech() {
                //
            }

            override fun onEndOfSpeech() {
                stopListening()
            }

            override fun onError(errorCode: Int) {
                val errorMessage = when (errorCode) {
                    SpeechRecognizer.ERROR_AUDIO -> applicationContext.getString(R.string.didnt_understand)
                    SpeechRecognizer.ERROR_NO_MATCH -> applicationContext.getString(R.string.no_match_found)
                    else -> ""
                }

                if (errorMessage.isNotEmpty()) {
                    onSpeechInputListener.showAlertMessage(errorMessage)
                }

                isUserSpeaking = false
                speechRecognizer.cancel()
                onSpeechInputListener.stopVoiceIconAnimation()
                Timber.e("<<<Voice $errorCode")
            }

            override fun onResults(bundle: Bundle?) {
                val results = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                isUserSpeaking = false

                if (results != null && results.isNotEmpty()) {
                    onSpeechInputListener.onTextResult(results[0])
                }
            }
        })
    }

    @UiThread
    fun destroy() {
        isUserSpeaking = false
        speechRecognizer.cancel()
        onSpeechInputListener.stopVoiceIconAnimation()
    }
}

class GoogleVoiceTypingDisabledException : Exception()
