package `in`.okcredit.voice_first.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import javax.inject.Inject

class BoosterVoiceCollectionTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Event {
        const val MIC_BUTTON_CLICKED = "Mic button clicked"
        const val RECORDING_STARTED = "Recording started"
        const val CANCEL_BUTTON_CLICKED = "Recording cancelled"
        const val RETRY_BUTTON_CLICKED = "Retry recording clicked"
        const val PERMISSION_REQUESTED = "Permission popup shown"
        const val PERMISSION_GRANTED = "Grant permission"
        const val PERMISSION_DENIED = "Denied permission"
        const val RECORDING_SUBMITTED = "Recording submitted"
    }

    object Property {
        const val SOURCE = "Source"
        const val INPUT_TEXT = "InputText"
        const val DURATION = "Duration"
        const val VALID = "Valid"
        const val REASON = "Reason"
        const val TYPE = "Type"
        const val STATE = "State"
        const val CAMPAIGN = "Campaign"
        const val SCREEN = "Screen"
    }

    object Value {
        const val SOURCE_BOOSTER_QUESTION = "OkPL Booster Question"
        const val MINIMUM_TIME = "Minimum time"
        const val MAXIMUM_TIME = "Maximum time"
        const val RECORD_AUDIO = "Record audio"
        const val CAMPAIGN = "Voice data collection"
        const val BOOSTER_VOICE_COLLECTION_SCREEN = "BoosterVoiceCollectionActivity"
    }

    fun micButtonClicked(state: String) {
        analyticsProvider.get().trackEvents(
            Event.MIC_BUTTON_CLICKED,
            mapOf(
                Property.STATE to state,
                Property.CAMPAIGN to Value.CAMPAIGN,
                Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN
            )
        )
    }

    fun recordingStarted() {
        analyticsProvider.get().trackEvents(
            Event.RECORDING_STARTED,
            mapOf(
                Property.CAMPAIGN to Value.CAMPAIGN,
                Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN
            )
        )
    }

    fun cancelButtonClicked() {
        analyticsProvider.get().trackEvents(
            Event.CANCEL_BUTTON_CLICKED,
            mapOf(Property.CAMPAIGN to Value.CAMPAIGN, Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN)
        )
    }

    fun retryButtonClicked() {
        analyticsProvider.get().trackEvents(
            Event.RETRY_BUTTON_CLICKED,
            mapOf(Property.CAMPAIGN to Value.CAMPAIGN, Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN)
        )
    }

    fun recordAudioPermissionRequested() {
        analyticsProvider.get().trackEvents(
            Event.PERMISSION_REQUESTED,
            mapOf(
                Property.TYPE to Value.RECORD_AUDIO,
                Property.CAMPAIGN to Value.CAMPAIGN,
                Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN
            )
        )
    }

    fun recordAudioPermissionGranted() {
        analyticsProvider.get().trackEvents(
            Event.PERMISSION_GRANTED,
            mapOf(
                Property.TYPE to Value.RECORD_AUDIO,
                Property.CAMPAIGN to Value.CAMPAIGN,
                Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN
            )
        )
    }

    fun recordAudioPermissionDenied() {
        analyticsProvider.get().trackEvents(
            Event.PERMISSION_DENIED,
            mapOf(
                Property.TYPE to Value.RECORD_AUDIO,
                Property.CAMPAIGN to Value.CAMPAIGN,
                Property.SCREEN to Value.BOOSTER_VOICE_COLLECTION_SCREEN
            )
        )
    }

    fun submitButtonClicked(isValid: Boolean, duration: Int, reason: String? = null) {
        val properties = HashMap<String, Any>().apply {
            this[Property.VALID] = isValid
            this[Property.DURATION] = duration
            reason?.let { this[Property.REASON] = reason }
            this[Property.CAMPAIGN] = Value.CAMPAIGN
            this[Property.SCREEN] = Value.BOOSTER_VOICE_COLLECTION_SCREEN
        }
        analyticsProvider.get().trackEvents(Event.RECORDING_SUBMITTED, properties)
    }
}
