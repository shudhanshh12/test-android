package `in`.okcredit.dynamicview.component.dashboard.banner_card

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BannerCardComponentModel(
    override val version: String,
    override val kind: String,
    override val metadata: ComponentModel.Metadata?,
    @Json(name = "event_handlers")
    override val eventHandlers: Map<String, Set<Action>>?,
    val title: String? = null,
    val subtitle: String? = null,
    val icon: String? = null,
    @Json(name = "bg_color")
    val bgColor: String?
) : ComponentModel {

    companion object {
        const val KIND = "banner_card"
    }
}
