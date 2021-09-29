package `in`.okcredit.dynamicview.component.dashboard.summary_card

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SummaryCardComponentModel(
    override val version: String,
    override val kind: String,
    override val metadata: ComponentModel.Metadata?,
    @Json(name = "event_handlers")
    override val eventHandlers: Map<String, Set<Action>>?,
    val title: String = "",
    val subtitle: String? = "",
    val icon: String? = "",
    @Transient val value: Long? = null,
    @Transient val valueDescription: String? = null
) : ComponentModel {

    companion object {
        const val KIND = "summary_card"
    }
}
