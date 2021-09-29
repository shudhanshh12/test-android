package merchant.okcredit.user_stories.server

import io.reactivex.Single

interface UserStoriesRemoteSource {
    fun getMyStory(startTime: Long? = null, businessId: String): Single<List<UserStoriesApiMessage.MyStory>>
    fun getOthersStory(startTime: Long? = null, businessId: String): Single<List<UserStoriesApiMessage.OthersStory>>
    fun postStory(addStory: UserStoriesApiMessage.AddMyStatus?, businessId: String): Single<UserStoriesApiMessage.MyStory>
}
