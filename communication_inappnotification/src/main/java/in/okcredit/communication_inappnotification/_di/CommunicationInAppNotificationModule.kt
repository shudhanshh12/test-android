package `in`.okcredit.communication_inappnotification._di

import `in`.okcredit.communication_inappnotification.BuildConfig
import `in`.okcredit.communication_inappnotification.contract.InAppNotification
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.remote.TapTarget
import `in`.okcredit.communication_inappnotification.contract.ui.remote.Tooltip
import `in`.okcredit.communication_inappnotification.local.InAppNotificationDatabase
import `in`.okcredit.communication_inappnotification.local.InAppNotificationDatabaseDao
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSource
import `in`.okcredit.communication_inappnotification.local.InAppNotificationLocalSourceImpl
import `in`.okcredit.communication_inappnotification.model.*
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationApiClient
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationRemoteSource
import `in`.okcredit.communication_inappnotification.remote.InAppNotificationRemoteSourceImpl
import `in`.okcredit.communication_inappnotification.ui.education_sheet.StandardEducationBottomSheet
import `in`.okcredit.communication_inappnotification.ui.education_sheet.TertiaryEducationBottomSheet
import `in`.okcredit.communication_inappnotification.usecase.DisplayStatusUpdater
import `in`.okcredit.communication_inappnotification.usecase.InAppNotificationRepositoryImpl
import `in`.okcredit.communication_inappnotification.usecase.InAppNotificationsSyncer
import `in`.okcredit.communication_inappnotification.usecase.LocalInAppNotificationHandlerImpl
import `in`.okcredit.communication_inappnotification.usecase.RemoteInAppNotificationHandlerImpl
import `in`.okcredit.communication_inappnotification.usecase.render.EducationSheetRenderer
import `in`.okcredit.communication_inappnotification.usecase.render.LocalInAppNotificationRenderer
import `in`.okcredit.communication_inappnotification.usecase.render.RemoteInAppNotificationRenderer
import `in`.okcredit.communication_inappnotification.usecase.render.TapTargetRenderer
import `in`.okcredit.communication_inappnotification.usecase.render.TooltipRenderer
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
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
import tech.okcredit.base.dagger.di.scope.FragmentScope

@Module
abstract class CommunicationInAppNotificationModule {

    @Binds
    @Reusable
    abstract fun getInAppNotificationForScreen(
        getInAppNotificationHandler: RemoteInAppNotificationHandlerImpl
    ): InAppNotificationHandler

    // Do not make this reusable because there are lifecycle aware functionalities attached to each instance
    @Binds
    abstract fun getNewLocalInAppNotificationRenderer(
        localInAppNotificationHandler: LocalInAppNotificationHandlerImpl,
    ): LocalInAppNotificationHandler

    @Binds
    @Reusable
    abstract fun syncer(syncer: InAppNotificationRepositoryImpl): InAppNotificationRepository

    @Binds
    @Reusable
    abstract fun store(inAppNotificationStore: InAppNotificationLocalSourceImpl): InAppNotificationLocalSource

    @Binds
    @Reusable
    abstract fun server(inAppNotificationServer: InAppNotificationRemoteSourceImpl): InAppNotificationRemoteSource

    @Binds
    @IntoMap
    @WorkerKey(InAppNotificationsSyncer.Worker::class)
    abstract fun syncWorker(factory: InAppNotificationsSyncer.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(DisplayStatusUpdater.Worker::class)
    abstract fun statusUpdateWorker(factory: DisplayStatusUpdater.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @StringKey(TooltipRenderer.REMOTE_NAME)
    @AppScope
    abstract fun renderTooltipRemote(renderTooltip: TooltipRenderer): RemoteInAppNotificationRenderer

    @Binds
    @IntoMap
    @StringKey(TapTargetRenderer.REMOTE_NAME)
    @AppScope
    abstract fun renderTapTargetRemote(renderTapTarget: TapTargetRenderer): RemoteInAppNotificationRenderer

    @Binds
    @IntoMap
    @StringKey(EducationSheetRenderer.NAME)
    @AppScope
    abstract fun renderEducationSheetRemote(renderEducationSheet: EducationSheetRenderer): RemoteInAppNotificationRenderer

    @Binds
    @IntoMap
    @StringKey(TooltipRenderer.LOCAL_NAME)
    @AppScope
    abstract fun renderTooltipLocal(renderTooltip: TooltipRenderer): LocalInAppNotificationRenderer

    @Binds
    @IntoMap
    @StringKey(TapTargetRenderer.LOCAL_NAME)
    @AppScope
    abstract fun renderTapTargetLocal(renderTapTarget: TapTargetRenderer): LocalInAppNotificationRenderer

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun educationSheetStandard(): StandardEducationBottomSheet

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun educationSheetTertiary(): TertiaryEducationBottomSheet

    companion object {

        @Provides
        fun database(context: Context, migrationsHandler: MultipleAccountsDatabaseMigrationHandler): InAppNotificationDatabase {
            return InAppNotificationDatabase.getInstance(context, migrationsHandler)
        }

        @Provides
        @Reusable
        fun dao(database: InAppNotificationDatabase): InAppNotificationDatabaseDao {
            return database.inAppNotificationDatabaseDao()
        }

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @CommunicationInAppNotification factory: MoshiConverterFactory
        ): InAppNotificationApiClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        @CommunicationInAppNotification
        internal fun moshiConverterFactory(@CommunicationInAppNotification moshi: Moshi) =
            MoshiConverterFactory.create(moshi)

        @Provides
        @CommunicationInAppNotification
        internal fun moshi(
            inAppNotificationAdapterFactory: InAppNotificationAdapterFactory,
            actionAdapterFactory: ActionAdapterFactory
        ): Moshi {
            return Moshi.Builder()
                .add(inAppNotificationAdapterFactory.newInstance())
                .add(actionAdapterFactory.newInstance())
                .build()
        }

        @Provides
        @IntoMap
        @StringKey(Tooltip.KIND)
        fun tooltipInAppNotification(): Class<out InAppNotification> = Tooltip::class.java

        @Provides
        @IntoMap
        @StringKey(TapTarget.KIND)
        fun tapTargetInAppNotification(): Class<out InAppNotification> = TapTarget::class.java

        @Provides
        @IntoMap
        @StringKey(EducationSheet.KIND)
        fun educationSheetInAppNotification(): Class<out InAppNotification> = EducationSheet::class.java

        @Provides
        @IntoMap
        @StringKey(Action.Track.ACTION)
        fun trackAction(): Class<out Action> = Action.Track::class.java

        @Provides
        @IntoMap
        @StringKey(Action.Navigate.ACTION)
        fun navigateAction(): Class<out Action> = Action.Navigate::class.java

        @Provides
        @IntoMap
        @StringKey(Action.Dismiss.ACTION)
        fun dismissAction(): Class<out Action> = Action.Dismiss::class.java
    }
}
