package tech.okcredit.userSupport.model

import androidx.annotation.Keep

@Keep
data class Help(
    val id: String,
    val icon: String?,
    val title: String?,
    val display_type: String?,
    val help_items: List<HelpItem>?
)
