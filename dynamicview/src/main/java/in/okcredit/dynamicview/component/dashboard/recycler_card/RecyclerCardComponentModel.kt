package `in`.okcredit.dynamicview.component.dashboard.recycler_card

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecyclerCardComponentModel(
    override val version: String,
    override val kind: String,
    override val metadata: ComponentModel.Metadata?,
    @Json(name = "event_handlers")
    override val eventHandlers: Map<String, Set<Action>>?,
    val items: List<ComponentModel>?,
    val title: String?,
    @Json(name = "cta_text")
    val ctaText: String?,
    @Json(name = "bg_color")
    val bgColor: String?
) : ComponentModel {

    class Kind(val value: String) {
        companion object {
            const val VERTICAL = "vertical_list_card"
            const val HORIZONTAL = "horizontal_list_card"
            const val GRID = "grid_card"

            const val DEFAULT_GRID_SPAN_COUNT = 3
        }
    }
}
