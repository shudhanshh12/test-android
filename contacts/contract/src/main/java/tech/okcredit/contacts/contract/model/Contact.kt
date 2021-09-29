package tech.okcredit.contacts.contract.model

import androidx.annotation.Keep

@Keep
data class Contact(
    val phonebookId: String,
    val name: String,
    val mobile: String,
    val picUri: String?,
    val found: Boolean = false,
    val timestamp: Long = 0L,
    val synced: Boolean,
    val type: Int = 0,
)
