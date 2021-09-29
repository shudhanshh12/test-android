package merchant.okcredit.user_stories.di

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import merchant.okcredit.user_stories.BuildConfig.USER_STORIES_URL
import merchant.okcredit.user_stories.UserStoryRepositoryImpl
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.homestory.HomeUserStoryLayout
import merchant.okcredit.user_stories.homestory.di.HomeUserStoryModule
import merchant.okcredit.user_stories.server.UserStoriesApiClient
import merchant.okcredit.user_stories.server.UserStoriesRemoteSource
import merchant.okcredit.user_stories.server.UserStoriesRemoteSourceImpl
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import merchant.okcredit.user_stories.store.UserStoriesLocalSourceImpl
import merchant.okcredit.user_stories.store.database.UserStoriesDao
import merchant.okcredit.user_stories.store.database.UserStoriesDatabase
import merchant.okcredit.user_stories.storycamera.UserStoryCameraActivity
import merchant.okcredit.user_stories.storypreview.StoryPreviewActivity
import merchant.okcredit.user_stories.storypreview.di.StoryPreviewActivityModule
import merchant.okcredit.user_stories.worker.UserStoriesAddStoryWorker
import merchant.okcredit.user_stories.worker.UserStoriesMyStatusWorker
import merchant.okcredit.user_stories.worker.UserStoriesOthersStatusWorker
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class UserStoriesModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [StoryPreviewActivityModule::class])
    abstract fun storyPreviewActivity(): StoryPreviewActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun storyCameraActivity(): UserStoryCameraActivity

    @Binds
    @Reusable
    abstract fun api(api: UserStoryRepositoryImpl): UserStoryRepository

    @Binds
    @IntoMap
    @WorkerKey(UserStoriesMyStatusWorker::class)
    @Reusable
    abstract fun userStoriesMyStatusWorker(factory: UserStoriesMyStatusWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(UserStoriesAddStoryWorker::class)
    @Reusable
    abstract fun userStoriesAddStoryWorker(factory: UserStoriesAddStoryWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(UserStoriesOthersStatusWorker::class)
    @Reusable
    abstract fun userStoriesOtherStatusWorker(factory: UserStoriesOthersStatusWorker.Factory): ChildWorkerFactory

    @ContributesAndroidInjector(modules = [HomeUserStoryModule::class])
    abstract fun homeUserStoryLayout(): HomeUserStoryLayout

    @Binds
    @Reusable
    abstract fun local(userStoriesLocal: UserStoriesLocalSourceImpl): UserStoriesLocalSource

    @Binds
    @Reusable
    abstract fun remote(userStoriesRemote: UserStoriesRemoteSourceImpl): UserStoriesRemoteSource

    companion object {

        @Provides
        @AppScope
        fun database(context: Context, multipleAccountMigrationHandler: MultipleAccountsDatabaseMigrationHandler): UserStoriesDatabase {
            return UserStoriesDatabase.getInstance(context, multipleAccountMigrationHandler)
        }

        @Provides
        fun dao(database: UserStoriesDatabase): UserStoriesDao = database.userStoriesDao()

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            factory: MoshiConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory,
        ): UserStoriesApiClient {
            return Retrofit.Builder()
                .baseUrl(USER_STORIES_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }

        @Provides
        @UserStories
        internal fun moshiConverterFactory(moshi: Moshi) =
            MoshiConverterFactory.create(moshi)

        @Provides
        @UserStories
        internal fun moshi() = Moshi.Builder().build()
    }
}
