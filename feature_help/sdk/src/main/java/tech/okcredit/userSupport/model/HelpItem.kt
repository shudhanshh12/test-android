package tech.okcredit.userSupport.model

import androidx.annotation.Keep

@Keep
data class HelpItem(
    val id: String,
    val title: String?,
    val sub_title: String?,
    val video_type: String?,
    val video_url: String?,
    val instructions: List<HelpInstruction>?
)
