package `in`.okcredit.dynamicview.component.dashboard.advertisement

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdvertisementComponentModel(
    override val version: String,
    override val kind: String,
    override val metadata: ComponentModel.Metadata?,
    @Json(name = "event_handlers")
    override val eventHandlers: Map<String, Set<Action>>?,
    val title: String = "",
    val subtitle: String? = "",
    val icon: String? = "",
    @Json(name = "button_text")
    val buttonText: String? = "",
    val image: String? = "",
    @Json(name = "bg_color")
    val bgColor: String?
) : ComponentModel {

    companion object {
        const val KIND = "advertisement"
    }
}
