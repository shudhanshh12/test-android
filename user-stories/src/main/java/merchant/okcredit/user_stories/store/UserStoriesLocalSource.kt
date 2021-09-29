package merchant.okcredit.user_stories.store

import `in`.okcredit.shared.utils.Timestamp
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.store.database.MyStory
import merchant.okcredit.user_stories.store.database.OthersStory

interface UserStoriesLocalSource {
    fun saveMyStory(data: List<MyStory>): Completable
    fun getLastSyncTimeMyStory(businessId: String): Single<Timestamp>
    fun saveOthersStory(data: List<OthersStory>): Completable
    fun getLastSyncTimeOthersStory(businessId: String): Single<Timestamp>
    fun getHomeUserStoryGroup(businessId: String): Observable<List<UserStories>>
    fun getMyStoryHome(businessId: String): Observable<List<MyStoryHome>>
    fun getOthersStoryByRelationShip(businessId: String): Single<List<OthersStory>>
    fun getUnSyncedStory(businessId: String): Single<List<MyStory>>
    fun getActiveCountMyStory(currentTimestamp: Long, businessId: String): Single<Int>
}
