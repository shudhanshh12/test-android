package merchant.okcredit.user_stories.store.database

import `in`.okcredit.shared.utils.Timestamp
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyStory(
    @PrimaryKey
    val requestId: String,
    val storyId: String? = null,
    val mediaId: String? = null,
    val mediaType: String,
    val medialLocalUrl: String? = null,
    val caption: String? = null,
    val views: Int = 0,
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val uploadAt: Timestamp? = null,
    val deleted: Boolean,
    val synced: Boolean = false,
    val imageUrlThumbnail: String? = null,
    val imageUrlMedium: String? = null,
    @ColumnInfo(index = true) val businessId: String,
)

@Entity
data class OthersStory(
    @PrimaryKey
    val storyId: String,
    val accountId: String? = null,
    val name: String? = null,
    val handle: String,
    val profilePic: String,
    val mediaId: String,
    val mediaType: String,
    val caption: String? = null,
    val relationship: String,
    val createdAt: Timestamp,
    val expiresAt: Timestamp,
    val viewed: Boolean,
    val deleted: Boolean,
    val link: String? = null,
    val mobile: String? = null,
    val imageUrlThumbnail: String? = null,
    val imageUrlMedium: String? = null,
    val localName: String? = null,
    val storyType: String,
    @ColumnInfo(index = true) val businessId: String,
)
