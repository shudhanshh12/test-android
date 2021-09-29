package `in`.okcredit.communication_inappnotification.model

import com.squareup.moshi.JsonClass

sealed class Action(val action: String) {

    @JsonClass(generateAdapter = true)
    data class Track(
        val event: String,
        val properties: Map<String, String> = mapOf()
    ) : Action(action = ACTION) {
        companion object {
            const val ACTION = "track"
        }
    }

    @JsonClass(generateAdapter = true)
    data class Navigate(
        val url: String?
    ) : Action(action = ACTION) {
        companion object {
            const val ACTION = "navigate"
        }
    }

    @JsonClass(generateAdapter = true)
    class Dismiss : Action(action = ACTION) {
        companion object {
            const val ACTION = "dismiss"
        }
    }

    companion object {
        const val KEY_POLYMORPHISM = "action"
    }
}
