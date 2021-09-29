package `in`.okcredit.dynamicview.data.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Customization(
    val target: String,
    val component: ComponentModel?
)
