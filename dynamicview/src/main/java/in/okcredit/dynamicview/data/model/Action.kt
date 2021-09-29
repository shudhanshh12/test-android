package `in`.okcredit.dynamicview.data.model

import com.squareup.moshi.JsonClass

sealed class Action(val action: String) {

    @JsonClass(generateAdapter = true)
    data class Track(
        val event: String,
        val properties: Map<String, String> = mapOf()
    ) : Action(action = NAME) {

        companion object {
            const val NAME = "track"
        }
    }

    @JsonClass(generateAdapter = true)
    data class Navigate(
        val url: String?
    ) : Action(action = NAME) {

        companion object {
            const val NAME = "navigate"
        }
    }

    companion object {
        const val KEY_POLYMORPHISM = "action"
    }
}

fun Action.Track.withDefaultProperties(targetName: String, component: ComponentModel): Action.Track {
    val props = properties.toMutableMap().apply {
        this["target"] = targetName
        this["component_version"] = component.version
        this["component_kind"] = component.kind
        this["name"] = component.metadata?.name ?: ""
        this["feature"] = component.metadata?.feature ?: ""
        this["lang"] = component.metadata?.lang ?: ""
    }
    return copy(event = event, properties = props)
}
