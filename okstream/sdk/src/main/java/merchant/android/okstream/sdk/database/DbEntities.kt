package merchant.android.okstream.sdk.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PublishMessage(
    @PrimaryKey
    val id: String,
    val topic: String,
    val message: String
)
