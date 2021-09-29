package `in`.okcredit.dynamicview.component.recycler

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecyclerComponentModel(
    override val version: String,
    override val kind: String,
    override val metadata: ComponentModel.Metadata?,
    @Json(name = "event_handlers")
    override val eventHandlers: Map<String, Set<Action>>?,
    val items: List<ComponentModel>?
) : ComponentModel {

    class Kind(val value: String) {
        companion object {
            const val VERTICAL = "vertical_list"
            const val HORIZONTAL = "horizontal_list"
            const val GRID = "grid"
        }
    }
}
