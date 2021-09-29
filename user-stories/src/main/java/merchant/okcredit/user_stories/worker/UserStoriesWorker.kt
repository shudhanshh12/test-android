package merchant.okcredit.user_stories.worker

import `in`.okcredit.merchant.contract.GetActiveBusiness
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import merchant.okcredit.user_stories.ApiEntityMapper
import merchant.okcredit.user_stories.analytics.UserStoriesTracker
import merchant.okcredit.user_stories.server.UserStoriesApiMessage
import merchant.okcredit.user_stories.server.UserStoriesRemoteSource
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import merchant.okcredit.user_stories.store.database.MyStory
import merchant.okcredit.user_stories.utils.UserStoriesFeature
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.contacts.contract.ContactsRepository
import timber.log.Timber
import javax.inject.Inject

class UserStoriesMyStatusWorker(
    private val context: Lazy<Context>,
    params: WorkerParameters,
    private val userStoriesRemoteSource: Lazy<UserStoriesRemoteSource>,
    private val tracker: Lazy<UserStoriesTracker>,
    private val userStoriesLocalSource: Lazy<UserStoriesLocalSource>,
    private val abRepository: Lazy<AbRepository>,
) : BaseRxWorker(context.get(), params) {
    companion object {
        const val BUSINESS_ID = "business_id"
    }

    override fun doRxWork(): Completable {
        val businessId = inputData.getString(BUSINESS_ID)!!
        return abRepository.get().isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES, businessId = businessId)
            .firstOrError()
            .flatMapCompletable { enabled ->
                if (enabled) {
                    userStoriesLocalSource.get().getLastSyncTimeMyStory(businessId).flatMapCompletable { timestamp ->
                        userStoriesRemoteSource.get().getMyStory(timestamp.epoch, businessId)
                            .flatMapCompletable {
                                tracker.get().trackMyStatusSync("1 My Status List from Server ", it.size)
                                saveMyStatusIntoDb(it, businessId)
                            }
                    }
                } else {
                    Timber.i("my status- user story feature disabled ")
                    Completable.complete()
                }
            }
    }

    private fun saveMyStatusIntoDb(myStoryList: List<UserStoriesApiMessage.MyStory>, businessId: String): Completable {
        return myStoryList.let {
            val list = it.map { remoteData -> ApiEntityMapper.MY_STORY(businessId).convert(remoteData) }
            tracker.get().trackMyStatusSync("2.My Status Locally saved ", list.size)
            userStoriesLocalSource.get().saveMyStory(list.requireNoNulls())
        }
    }

    class Factory @Inject constructor(
        private val localSource: Lazy<UserStoriesLocalSource>,
        private val remoteSource: Lazy<UserStoriesRemoteSource>,
        private val tracker: Lazy<UserStoriesTracker>,
        private val abRepository: Lazy<AbRepository>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return UserStoriesMyStatusWorker(
                context = { context },
                params = params,
                userStoriesLocalSource = localSource,
                userStoriesRemoteSource = remoteSource,
                tracker = tracker,
                abRepository = abRepository,
            )
        }
    }
}

class UserStoriesOthersStatusWorker(
    private val context: Lazy<Context>,
    params: WorkerParameters,
    private val userStoriesRemoteSource: Lazy<UserStoriesRemoteSource>,
    private val tracker: Lazy<UserStoriesTracker>,
    private val userStoriesLocalSource: Lazy<UserStoriesLocalSource>,
    private val contactsRepository: Lazy<ContactsRepository>,
    private val abRepository: Lazy<AbRepository>,
) : BaseRxWorker(context.get(), params) {
    companion object {
        const val BUSINESS_ID = "business_id"
    }

    override fun doRxWork(): Completable {
        val businessId = inputData.getString(UserStoriesMyStatusWorker.BUSINESS_ID)!!
        return abRepository.get().isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES, businessId = businessId)
            .firstOrError()
            .flatMapCompletable { featureEnable ->
                if (featureEnable) {
                    userStoriesLocalSource.get().getLastSyncTimeOthersStory(businessId)
                        .flatMapCompletable { timestamp ->
                            userStoriesRemoteSource.get().getOthersStory(timestamp.epoch, businessId)
                                .flatMapCompletable {
                                    tracker.get().trackOthersStatusSync("1 Other Status List from Server ", it.size)
                                    saveOtherStatusIntoDb(it, businessId)
                                        .andThen(updateLocalContactsName(businessId))
                                }
                        }
                } else {
                    Timber.i("other status- user story feature disabled ")
                    Completable.complete()
                }
            }
    }

    private fun updateLocalContactsName(businessId: String): Completable {
        return contactsRepository.get().getContacts().flatMapCompletable { contacts ->
            val map = contacts.associateBy { it.mobile }
            val otherStatus = userStoriesLocalSource.get().getOthersStoryByRelationShip(businessId)
            otherStatus.flatMapCompletable { otherStatusList ->
                val updatedStatus = otherStatusList.map {
                    it.copy(localName = map[it.mobile]?.name)
                }
                tracker.get().trackOthersStatusSync(
                    "3 Other Status List updated with local contact name ",
                    updatedStatus.size
                )
                userStoriesLocalSource.get().saveOthersStory(updatedStatus)
            }
        }
    }

    private fun saveOtherStatusIntoDb(otherStoryList: List<UserStoriesApiMessage.OthersStory?>, businessId: String): Completable {
        return otherStoryList.let {
            val list = it.map { remoteData -> ApiEntityMapper.OTHERS_STORY(businessId).convert(remoteData) }
            tracker.get().trackOthersStatusSync("2.Others Status Locally saved ", list.size)
            userStoriesLocalSource.get().saveOthersStory(list.requireNoNulls())
        }
    }

    class Factory @Inject constructor(
        private val localSource: Lazy<UserStoriesLocalSource>,
        private val remoteSource: Lazy<UserStoriesRemoteSource>,
        private val tracker: Lazy<UserStoriesTracker>,
        private val contactsRepository: Lazy<ContactsRepository>,
        private val abRepository: Lazy<AbRepository>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return UserStoriesOthersStatusWorker(
                context = { context },
                params = params,
                userStoriesLocalSource = localSource,
                userStoriesRemoteSource = remoteSource,
                tracker = tracker,
                contactsRepository = contactsRepository,
                abRepository = abRepository,
            )
        }
    }
}

class UserStoriesAddStoryWorker(
    private val context: Lazy<Context>,
    params: WorkerParameters,
    private val userStoriesRemoteSource: Lazy<UserStoriesRemoteSource>,
    private val tracker: Lazy<UserStoriesTracker>,
    private val userStoriesLocalSource: Lazy<UserStoriesLocalSource>,
    private val abRepository: Lazy<AbRepository>,
    private val activeBusiness: Lazy<GetActiveBusiness>,
) : BaseRxWorker(context.get(), params) {
    companion object {
        const val BUSINESS_ID = "business_id"
    }

    override fun doRxWork(): Completable {
        val businessId = inputData.getString(BUSINESS_ID)!!
        return abRepository.get().isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES, businessId = businessId)
            .firstOrError()
            .flatMapCompletable { enabled ->
                if (enabled) {
                    userStoriesLocalSource.get().getUnSyncedStory(businessId).toObservable().flatMapIterable { it }
                        .flatMap {
                            postStoryToServer(it, businessId).toObservable()
                        }.flatMapCompletable {
                            saveMyStatusIntoDb(listOf(it), businessId)
                            activeBusiness.get().execute().firstOrError().flatMapCompletable { business ->
                                it.expires_at?.let { it1 ->
                                    tracker.get().trackEventStoryUploadStorySuccess(
                                        business.id,
                                        it1,
                                        it.caption ?: ""
                                    )
                                }
                                Completable.complete()
                            }
                        }
                } else {
                    Timber.i("my status- user story feature disabled ")
                    Completable.complete()
                }
            }
    }

    private fun postStoryToServer(it: MyStory, businessId: String): Single<UserStoriesApiMessage.MyStory> {
        val addStory = ApiEntityMapper.ADD_STORY(businessId).reverse().convert(it)
        return userStoriesRemoteSource.get().postStory(addStory, businessId)
    }

    private fun saveMyStatusIntoDb(myStoryList: List<UserStoriesApiMessage.MyStory>, businessId: String): Completable {
        return myStoryList.let {
            val list = it.map { remoteData -> ApiEntityMapper.MY_STORY(businessId).convert(remoteData) }
            tracker.get().trackMyStatusSync("2.My Status Locally saved ", list.size)
            userStoriesLocalSource.get().saveMyStory(list.requireNoNulls())
        }
    }

    class Factory @Inject constructor(
        private val localSource: Lazy<UserStoriesLocalSource>,
        private val remoteSource: Lazy<UserStoriesRemoteSource>,
        private val tracker: Lazy<UserStoriesTracker>,
        private val abRepository: Lazy<AbRepository>,
        private val activeBusiness: Lazy<GetActiveBusiness>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return UserStoriesAddStoryWorker(
                context = { context },
                params = params,
                userStoriesLocalSource = localSource,
                userStoriesRemoteSource = remoteSource,
                tracker = tracker,
                abRepository = abRepository,
                activeBusiness = activeBusiness,
            )
        }
    }
}
