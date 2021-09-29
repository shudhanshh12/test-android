package merchant.okcredit.user_stories.store

import `in`.okcredit.shared.utils.Timestamp
import androidx.room.EmptyResultSetException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.store.database.MyStory
import merchant.okcredit.user_stories.store.database.OthersStory
import merchant.okcredit.user_stories.store.database.UserStoriesDao
import tech.okcredit.android.base.utils.ThreadUtils
import javax.inject.Inject

class UserStoriesLocalSourceImpl @Inject constructor(private val userStoriesDao: UserStoriesDao) :
    UserStoriesLocalSource {
    override fun saveMyStory(data: List<MyStory>): Completable = userStoriesDao.insertMyStory(data)

    override fun getLastSyncTimeMyStory(businessId: String): Single<Timestamp> {
        return userStoriesDao.getLastSyncMyStoryTimestamp(businessId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker()).onErrorResumeNext {
                if (it is EmptyResultSetException) return@onErrorResumeNext Single.just(Timestamp(0L))
                else throw it
            }
    }

    override fun saveOthersStory(data: List<OthersStory>): Completable = userStoriesDao.insertOtherStory(data)

    override fun getLastSyncTimeOthersStory(businessId: String): Single<Timestamp> {
        return userStoriesDao.getLastSyncOtherStoryTimestamp(businessId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException) return@onErrorResumeNext Single.just(Timestamp(0L))
                else throw it
            }
    }

    override fun getHomeUserStoryGroup(businessId: String): Observable<List<UserStories>> {
        return userStoriesDao.getDistinctOtherStoryGroup(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getMyStoryHome(businessId: String): Observable<List<MyStoryHome>> {
        return userStoriesDao.getDistinctMyStoryHome(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getOthersStoryByRelationShip(businessId: String): Single<List<OthersStory>> {
        return userStoriesDao.getOthersStoryByRelationShip(businessId = businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException) return@onErrorResumeNext Single.just(listOf<OthersStory>())
                else throw it
            }
    }

    override fun getUnSyncedStory(businessId: String): Single<List<MyStory>> {
        return userStoriesDao.getUnSyncedMyStory(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException) return@onErrorResumeNext Single.just(listOf<MyStory>())
                else throw it
            }
    }

    override fun getActiveCountMyStory(currentTimestamp: Long, businessId: String): Single<Int> {
        return userStoriesDao.getActiveCountMyStory(currentTimestamp, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException) return@onErrorResumeNext Single.just(0)
                else throw it
            }
    }
}
