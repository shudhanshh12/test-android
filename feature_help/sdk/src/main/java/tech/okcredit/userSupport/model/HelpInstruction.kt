package tech.okcredit.userSupport.model

import androidx.annotation.Keep

@Keep
data class HelpInstruction(
    val id: String,
    val image_url: String?,
    val title: String?,
    val type: String?
)
