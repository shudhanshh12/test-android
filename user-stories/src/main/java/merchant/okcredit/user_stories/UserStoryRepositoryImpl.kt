package merchant.okcredit.user_stories

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import merchant.okcredit.user_stories.worker.UserStoriesAddStoryWorker
import merchant.okcredit.user_stories.worker.UserStoriesMyStatusWorker
import merchant.okcredit.user_stories.worker.UserStoriesOthersStatusWorker
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserStoryRepositoryImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val userStoryLocalSource: Lazy<UserStoriesLocalSource>,
) :
    UserStoryRepository {
    companion object {
        const val WORKER_MY_STORY = "worker_my_story"
        const val WORKER_ADD_STORY = "sync_add_story"
        const val WORKER_OTHERS_STORY = "worker_others_story"
    }

    override fun scheduleSyncStories(businessId: String): Completable {
        return syncMyStory(businessId)
            .andThen(syncOtherStory(businessId))
            .andThen(syncAddStory(businessId))
    }

    override fun syncMyStory(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_MY_STORY
            val workRequest = OneTimeWorkRequestBuilder<UserStoriesMyStatusWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        UserStoriesMyStatusWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override fun syncOtherStory(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_OTHERS_STORY
            val workRequest = OneTimeWorkRequestBuilder<UserStoriesOthersStatusWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        UserStoriesOthersStatusWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override fun syncAddStory(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_ADD_STORY
            val workRequest = OneTimeWorkRequestBuilder<UserStoriesAddStoryWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        UserStoriesAddStoryWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override fun getGroupUserStories(businessId: String): Observable<List<UserStories>> {
        return userStoryLocalSource.get().getHomeUserStoryGroup(businessId)
    }

    override fun getMyStoryHome(businessId: String): Observable<List<MyStoryHome>> {
        return userStoryLocalSource.get().getMyStoryHome(businessId)
    }
}
