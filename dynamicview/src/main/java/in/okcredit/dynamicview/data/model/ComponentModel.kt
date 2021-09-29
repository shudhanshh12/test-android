package `in`.okcredit.dynamicview.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface ComponentModel {
    val version: String
    val kind: String
    val metadata: Metadata?

    @Json(name = "event_handlers")
    val eventHandlers: Map<String, Set<Action>>?

    companion object {
        const val KEY_POLYMORPHISM = "kind"
    }

    @JsonClass(generateAdapter = true)
    data class Metadata(
        val name: String?,
        val feature: String?,
        val lang: String?,
        val duration: Int? = null,
        @Json(name = "item_kind")
        val itemKind: String? = null,
        @Json(name = "span_count")
        val spanCount: Int? = null,
        @Json(name = "item_count")
        val itemCount: Int? = null
    )
}
