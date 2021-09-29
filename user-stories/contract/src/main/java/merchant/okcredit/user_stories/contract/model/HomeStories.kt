package merchant.okcredit.user_stories.contract.model

data class HomeStories(
    val userStories: List<UserStories>,
    val isMyStoryAdded: Boolean,
    val isAllSynced: Boolean,
    val lastStoryUrl: String?
)

data class UserStories(
    val id: String,
    val type: String,
    val leastUnseenImageUrl: String?,
    val totalStories: Int,
    val totalSeen: Int,
    val name: String,
    val recentCreatedAt: String,
    val storyId: String,
    val allViewed: Int,
    val relationship: String,
    val storyType: String,
)

data class MyStoryHome(
    val latestImageUrl: String,
    val allSynced: Boolean,
    val createdAt: String,
    val isMyStoryAdded: Boolean = true
)

object StoriesConstants {
    const val RELATIONSHIP_KNOWN = "KNOWN" // for customer or supplier
    const val RELATIONSHIP_UNKNOWN = "UNKNOWN" // none
    const val HANDLE_VENDOR = "VENDOR"
    const val HANDLE_USER = "USER"
}
