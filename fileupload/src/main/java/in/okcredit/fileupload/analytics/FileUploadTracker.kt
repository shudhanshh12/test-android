package `in`.okcredit.fileupload.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.fileupload.analytics.FileUploadTracker.Event.UPLOAD_AUDIO_ERROR
import dagger.Lazy
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

class FileUploadTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Key {
        const val LANG = "stt_input_lang"
        const val REASON = "Reason"
        const val STACKTRACE = "StackTrace"
        const val CAUSE = "Cause"
        const val TYPE = "type"
    }

    object Event {
        const val UPLOAD_AUDIO_WORKER_SCHEDULED = "Upload Audio Worker Scheduled"
        const val UPLOAD_AUDIO_WORKER_STARTED = "Upload Audio Worker Started"
        const val UPLOAD_AUDIO_SUCCESS = "Upload Audio Success"
        const val UPLOAD_AUDIO_ERROR = "Upload Audio Error"
    }

    fun trackUploadAudioStatus(event: String, trackerProperties: Map<String, String>) =
        analyticsProvider.get().trackEngineeringMetricEvents(event, trackerProperties)

    fun trackUploadAudioError(
        throwable: Throwable,
        trackerProperties: Map<String, String>,
    ) {
        val properties = HashMap<String, String>().apply {
            putAll(trackerProperties)
            this[Key.REASON] = throwable.message.itOrBlank()
            this[Key.STACKTRACE] = throwable.getStringStackTrace()
            this[Key.CAUSE] = throwable.cause?.message.itOrBlank()
        }
        analyticsProvider.get().trackEngineeringMetricEvents(UPLOAD_AUDIO_ERROR, properties)
    }
}
