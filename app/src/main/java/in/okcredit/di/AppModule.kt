package `in`.okcredit.di

import `in`.okcredit.BuildConfig
import `in`.okcredit.LegacyNavigatorImpl
import `in`.okcredit.R
import `in`.okcredit.backend._offline.usecase.CheckAuthImpl
import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.backend.contract.Constants
import `in`.okcredit.di.binding.communications.GetCustomerBindingImpl
import `in`.okcredit.di.binding.communications.GetDailyReportImpl
import `in`.okcredit.di.binding.communications.GetNotificationIntentBindingImpl
import `in`.okcredit.di.binding.communications.GetSupplierBindingImpl
import `in`.okcredit.di.binding.communications.GetSyncNotificationJobBindingImpl
import `in`.okcredit.di.binding.network.TrackerNetworkPerformanceImpl
import `in`.okcredit.dynamicview.data.model.ActionAdapterFactory
import `in`.okcredit.dynamicview.data.model.ComponentAdapterFactory
import `in`.okcredit.frontend.usecase.AuthenticateImpl
import `in`.okcredit.home.HomeNavigator
import `in`.okcredit.home.IGetRelationsNumbersAndBalance
import `in`.okcredit.home.ILanguageNudgeHelper
import `in`.okcredit.notification.ResolveIntentsAndExtrasFromDeeplinkImpl
import `in`.okcredit.shortcut.AppShortcutAdderImpl
import `in`.okcredit.upgrade.AppUpgradeReceiver
import `in`.okcredit.upgrade.AppUpgradeWorker
import `in`.okcredit.util.FirebaseRemoteConfigDefaults
import `in`.okcredit.util.FirebaseRemoteConfigDefaults.Companion.DEFAULT_FIREBASE_REMOTE_SYNC_INTERVAL
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.speech.SpeechRecognizer
import androidx.annotation.IdRes
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig
import com.github.anrwatchdog.ANRWatchDog
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.scottyab.rootbeer.RootBeer
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.converter.moshi.MoshiConverterFactory
import tech.okcredit.account_chat_contract.ChatNavigator
import tech.okcredit.account_chat_ui.ChatNavigatorImpl
import tech.okcredit.android.base.AppConfig
import tech.okcredit.android.base.AppVariable
import tech.okcredit.android.base.AppVariableImpl
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.moshi.DateTimeAdapter
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.android.communication.GetCustomerBinding
import tech.okcredit.android.communication.GetDailyReportDetailsBinding
import tech.okcredit.android.communication.GetNotificationIntentBinding
import tech.okcredit.android.communication.GetSupplierBinding
import tech.okcredit.android.communication.GetSyncNotificationJobBinding
import tech.okcredit.app_contract.AppShortcutAdder
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.app_contract.ResolveIntentsAndExtrasFromDeeplink
import tech.okcredit.base.network.utils.TrackNetworkPerformanceBinding
import tech.okcredit.bill_management_ui.BillsNavigatorImpl
import tech.okcredit.bills.BillsNavigator
import tech.okcredit.bills.IGetAccountsTotalBills
import tech.okcredit.home.HomeNavigatorImpl
import tech.okcredit.home.ui.home.helpers.LanguageNudgeHelper
import tech.okcredit.home.usecase.GetRelationsNumbersAndBalanceImpl
import tech.okcredit.secure_keys.KeyProvider
import tech.okcredit.use_case.GetAccountsTotalBills

@Module
abstract class AppModule {

    @Binds
    @AppScope
    abstract fun legacyNavigator(navigator: LegacyNavigatorImpl): LegacyNavigator

    @ContributesAndroidInjector
    abstract fun appUpgradeReceiver(): AppUpgradeReceiver

    @Binds
    @IntoMap
    @WorkerKey(AppUpgradeWorker::class)
    abstract fun appUpgradeWorker(factory: AppUpgradeWorker.Factory): ChildWorkerFactory

    @Binds
    @Reusable
    abstract fun getCustomerBinding(getCustomerBindingImpl: GetCustomerBindingImpl): GetCustomerBinding

    @Binds
    @Reusable
    abstract fun getSupplierBinding(getSupplierBindingImpl: GetSupplierBindingImpl): GetSupplierBinding

    @Binds
    @AppScope
    abstract fun getSyncNotificationJobBinding(GetSyncNotificationJobBinding: GetSyncNotificationJobBindingImpl): GetSyncNotificationJobBinding

    @Binds
    @Reusable
    abstract fun getDailyReportDetailsBinding(getDailyReport: GetDailyReportImpl): GetDailyReportDetailsBinding

    @Binds
    @Reusable
    abstract fun getNotificationIntentBinding(getNotificationIntentBinding: GetNotificationIntentBindingImpl): GetNotificationIntentBinding

    @Binds
    @AppScope
    abstract fun trackNetworkPerformanceBinding(trackerNetworkPerformanceImpl: TrackerNetworkPerformanceImpl): TrackNetworkPerformanceBinding

    @Binds
    @AppScope
    abstract fun deeplinkUtils(resolveIntentsAndExtrasFromDeeplinkImpl: ResolveIntentsAndExtrasFromDeeplinkImpl): ResolveIntentsAndExtrasFromDeeplink

    @Binds
    @Reusable
    abstract fun authenticate(authenticate: AuthenticateImpl): Authenticate

    @Binds
    @Reusable
    abstract fun checkAuth(checkAuthImpl: CheckAuthImpl): CheckAuth

    @Binds
    @Reusable
    abstract fun appShortcutAdder(appShortcutAdder: AppShortcutAdderImpl): AppShortcutAdder

    @Binds
    @AppScope
    abstract fun homeNavigator(homeNavigator: HomeNavigatorImpl): HomeNavigator

    @Binds
    abstract fun languageNudgeHelper(languageNudgeHelper: LanguageNudgeHelper): ILanguageNudgeHelper

    @Binds
    abstract fun getAccountsTotalBills(getAccountsTotalBills: GetAccountsTotalBills): IGetAccountsTotalBills

    @Binds
    abstract fun getRelationsNumbers(getRelationsNumbersAndBalance: GetRelationsNumbersAndBalanceImpl): IGetRelationsNumbersAndBalance

    @Binds
    abstract fun chatNavigator(chatNavigator: ChatNavigatorImpl): ChatNavigator

    @Binds
    abstract fun billsNavigator(billsNavigator: BillsNavigatorImpl): BillsNavigator

    companion object {

        @Provides
        @AppScope
        fun applicationContext(app: Application): Context = app

        @Provides
        @AppScope
        fun mixpanelAPI(app: Application) = MixpanelAPI.getInstance(app, Constants.MIXPANEL_TOKEN)

        @Provides
        @AppScope
        fun speechRecognizer(context: Context): SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        @Provides
        @AppScope
        fun cleverTapAPI(app: Application): CleverTapAPI {

            val config = if (AppConfig.FLAVOR_SERVER_STAGING == BuildConfig.FLAVOR) {
                CleverTapInstanceConfig.createInstance(
                    app,
                    KeyProvider.getCleverTapAccountIdStaging(),
                    KeyProvider.getCleverTapTokenStaging()
                )
            } else if (AppConfig.FLAVOR_SERVER_ALPHA == BuildConfig.FLAVOR) {
                CleverTapInstanceConfig.createInstance(
                    app,
                    KeyProvider.getCleverTapAccountIdAlpha(),
                    KeyProvider.getCleverTapTokenAlpha()
                )
            } else {
                CleverTapInstanceConfig.createInstance(
                    app,
                    KeyProvider.getCleverTapAccountIdProd(),
                    KeyProvider.getCleverTapTokenProd()
                )
            }

            return CleverTapAPI.instanceWithConfig(app, config)
        }

        @Provides
        fun firebaseAnalytics(app: Application) = FirebaseAnalytics.getInstance(app)

        @Provides
        fun firebaseCrashlytics() = FirebaseCrashlytics.getInstance()

        @Provides
        fun fireAuth() = FirebaseAuth.getInstance()

        @Provides
        fun firebaseFirestore() = FirebaseFirestore.getInstance()

        @Provides
        @AppScope
        fun firebaseRemoteConfig(defaults: Lazy<FirebaseRemoteConfigDefaults>): FirebaseRemoteConfig {
            val configSettings = FirebaseRemoteConfigSettings.Builder().apply {
                minimumFetchIntervalInSeconds = DEFAULT_FIREBASE_REMOTE_SYNC_INTERVAL
            }.build()

            return FirebaseRemoteConfig.getInstance().apply {
                setConfigSettingsAsync(configSettings)
                setDefaultsAsync(defaults.get().getDefaultsMap())
            }
        }

        @Provides
        @AppScope
        fun appUpdateManager(app: Application): AppUpdateManager = AppUpdateManagerFactory.create(app)

        @Provides
        @UiThread
        fun uiScheduler(): Scheduler = AndroidSchedulers.mainThread()

        @Provides
        @AppScope
        fun appVariable(): AppVariable {
            return AppVariableImpl
        }

        @Provides
        @AppScope
        fun provideActivityManager(app: Application): ActivityManager {
            return app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }

        @Provides
        fun firebasePerformance() = FirebasePerformance.getInstance()

        @Provides
        fun anrWatchDog() = ANRWatchDog()

        @Provides
        fun moshiConverterFactory(): MoshiConverterFactory = MoshiConverterFactory.create()

        @Provides
        fun rootBeer(context: Context) = RootBeer(context)

        @Provides
        fun moshi(
            componentAdapterFactory: ComponentAdapterFactory,
            actionAdapterFactory: ActionAdapterFactory,
        ): Moshi {
            return Moshi.Builder()
                .add(actionAdapterFactory.newInstance())
                .add(componentAdapterFactory.newInstance())
                .build()
        }

        @Provides
        @AppScope
        fun dateTimeAdapter() = DateTimeAdapter()

        @Provides
        fun gson() = Gson()

        @Provides
        fun connectivityManager(context: Context) =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        @Provides
        @IdRes
        fun fragmentContainerView(): Int = R.id.nav_host_fragment
    }
}
