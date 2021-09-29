package `in`.okcredit.dynamicview.component.dashboard.cell2

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Cell2ComponentModel(
    override val version: String,
    override val kind: String,
    override val metadata: ComponentModel.Metadata?,
    @Json(name = "event_handlers")
    override val eventHandlers: Map<String, Set<Action>>?,
    val title: String? = null,
    val subtitle: String? = null,
    val icon: String? = null
) : ComponentModel {

    companion object {
        const val KIND = "cell2"
    }
}
