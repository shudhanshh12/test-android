package `in`.okcredit.communication_inappnotification.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ActionButton(
    val text: String,
    val iconUrl: String? = null,
    val clickHandlers: Set<Action>? = null
)
