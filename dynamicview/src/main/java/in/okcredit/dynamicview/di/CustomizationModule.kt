package `in`.okcredit.dynamicview.di

import `in`.okcredit.dynamicview.BuildConfig
import `in`.okcredit.dynamicview.component.banner.BannerComponentModule
import `in`.okcredit.dynamicview.component.cell.CellComponentModule
import `in`.okcredit.dynamicview.component.dashboard.advertisement.AdvertisementComponentModule
import `in`.okcredit.dynamicview.component.dashboard.banner_card.BannerCardComponentModule
import `in`.okcredit.dynamicview.component.dashboard.cell2.Cell2ComponentModule
import `in`.okcredit.dynamicview.component.dashboard.cell_card.CellCardComponentModule
import `in`.okcredit.dynamicview.component.dashboard.recycler_card.RecyclerCardComponentModule
import `in`.okcredit.dynamicview.component.dashboard.summary_card.SummaryCardComponentModule
import `in`.okcredit.dynamicview.component.menu.MenuComponentModule
import `in`.okcredit.dynamicview.component.recycler.RecyclerComponentModule
import `in`.okcredit.dynamicview.component.toolbar.ToolbarComponentModule
import `in`.okcredit.dynamicview.data.CustomizationSyncWorker
import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ActionAdapterFactory
import `in`.okcredit.dynamicview.data.model.ComponentAdapterFactory
import `in`.okcredit.dynamicview.data.server.CustomizationApiService
import `in`.okcredit.dynamicview.data.store.database.CustomizationDatabase
import `in`.okcredit.dynamicview.data.store.database.CustomizationDatabaseDao
import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@Module(
    includes = [
        MenuComponentModule::class,
        RecyclerComponentModule::class,
        BannerComponentModule::class,
        CellComponentModule::class,
        ToolbarComponentModule::class,
        SummaryCardComponentModule::class,
        AdvertisementComponentModule::class,
        RecyclerCardComponentModule::class,
        Cell2ComponentModule::class,
        CellCardComponentModule::class,
        BannerCardComponentModule::class
    ]
)
abstract class CustomizationModule {

    @Binds
    @IntoMap
    @WorkerKey(CustomizationSyncWorker::class)
    abstract fun dynamicViewSyncWorker(factory: CustomizationSyncWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(Action.Track.NAME)
        fun trackAction(): Class<out Action> = Action.Track::class.java

        @Provides
        @IntoMap
        @StringKey(Action.Navigate.NAME)
        fun navigateAction(): Class<out Action> = Action.Navigate::class.java

        @Provides
        @DynamicView
        fun moshiConverterFactory(@DynamicView moshi: Moshi) = MoshiConverterFactory.create(moshi)

        @Provides
        @DynamicView
        fun moshi(
            componentAdapterFactory: ComponentAdapterFactory,
            actionAdapterFactory: ActionAdapterFactory
        ): Moshi {
            return Moshi.Builder()
                .add(actionAdapterFactory.newInstance())
                .add(componentAdapterFactory.newInstance())
                .build()
        }

        @Provides
        fun customizationApiService(
            @AuthOkHttpClient client: Lazy<OkHttpClient>,
            @DynamicView converterFactory: MoshiConverterFactory
        ): CustomizationApiService {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.DYNAMIC_UI_URL)
                .delegatingCallFactory(client)
                .addConverterFactory(converterFactory)
                .build()
                .create()
        }

        @Provides
        @AppScope
        fun database(context: Context): CustomizationDatabase {
            return CustomizationDatabase.getInstance(context)
        }

        @Provides
        @Reusable
        fun dao(database: CustomizationDatabase): CustomizationDatabaseDao = database.customizationDatabaseDao()
    }
}
