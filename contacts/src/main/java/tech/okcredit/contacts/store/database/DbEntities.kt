package tech.okcredit.contacts.store.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    @PrimaryKey
    val mobile: String,
    val phoneBookId: String,
    val name: String? = null,
    val picUri: String?,
    val found: Boolean = false,
    val timestamp: Long = 0L,
    val synced: Boolean,
    val type: Int = 0
)
