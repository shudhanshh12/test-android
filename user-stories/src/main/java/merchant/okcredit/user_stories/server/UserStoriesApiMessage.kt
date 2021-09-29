package merchant.okcredit.user_stories.server

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface UserStoriesApiMessage {

    @Keep
    @JsonClass(generateAdapter = true)
    data class MyStory(
        val request_id: String? = null,
        val status_id: String,
        val media_id: String? = null,
        val media_type: String,
        val caption: String? = null,
        val views: Int = 0,
        val created_at: String? = null,
        val expires_at: String? = null,
        val deleted: Boolean,
        val image_url_thumbnail: String? = null,
        val image_url_medium: String? = null,
        val urls: Urls,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class OthersStory(
        @Json(name = "status_id")
        val storyId: String,
        val account_id: String?,
        val name: String? = null,
        val handle: String,
        val profile_pic: String,
        val media_id: String,
        val media_type: String,
        val caption: String,
        val relationship: String,
        val created_at: String,
        val expires_at: String,
        val viewed: Boolean,
        val deleted: Boolean,
        val link: String?,
        val mobile: String?,
        val image_url_thumbnail: String? = null,
        val image_url_medium: String? = null,
        val urls: Urls,
        @Json(name = "status_type")
        val storyType: String,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class Urls(
        val thumbnail: String,
        val medium: String,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class UserStatusListResponse<T>(
        val response: List<T>,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class UserStatusResponse<T>(
        val response: T,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class AddMyStatus(
        val request_id: String,
        val caption: String?,
        val media_url: String,
        val updatedAt: Long,
    )
}
