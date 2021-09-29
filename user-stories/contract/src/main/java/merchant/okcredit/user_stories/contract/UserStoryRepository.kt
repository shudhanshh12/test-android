package merchant.okcredit.user_stories.contract

import io.reactivex.Completable
import io.reactivex.Observable
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.UserStories

interface UserStoryRepository {
    fun scheduleSyncStories(businessId: String): Completable
    fun syncMyStory(businessId: String): Completable
    fun syncOtherStory(businessId: String): Completable
    fun getGroupUserStories(businessId: String): Observable<List<UserStories>>
    fun getMyStoryHome(businessId: String): Observable<List<MyStoryHome>>
    fun syncAddStory(businessId: String): Completable
}
