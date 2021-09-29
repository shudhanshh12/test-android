package `in`.okcredit

import `in`.okcredit.analytics.*
import `in`.okcredit.analytics.helpers.AnalyticsNotificationHelper
import `in`.okcredit.backend.analytics.DebugAnalyticsProvider
import `in`.okcredit.backend.analytics.appsflyer.AppsFlyerAnalyticsProvider
import `in`.okcredit.backend.analytics.clevertap.ClevertapAnalyticsProvider
import `in`.okcredit.backend.analytics.crashlytics.CrashlyticsAnalyticsHelper
import `in`.okcredit.backend.analytics.firebase.FirebaseAnalyticsProvider
import `in`.okcredit.backend.analytics.mixpanel.MixpanelAnalyticsProvider
import `in`.okcredit.backend.contract.AppLockManager
import `in`.okcredit.backend.contract.ServerConfigManager
import `in`.okcredit.backend.utils.FileUtils
import `in`.okcredit.di.AppComponent
import `in`.okcredit.di.DaggerAppComponent
import `in`.okcredit.frontend.common.DebugLoggingTree
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.merchant.device.Referrer
import `in`.okcredit.merchant.device.ReferrerSource
import `in`.okcredit.merchant.device.nougat
import `in`.okcredit.onboarding.contract.marketing.AppsFlyerHelper
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.shared.performance.app_startup.AppStartMeasureLifeCycleCallBacks
import `in`.okcredit.shared.performance.app_startup.AppStartUpMeasurementUtils
import `in`.okcredit.shared.performance.app_startup.StartUpMeasurementDataObject
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.ui._utils.GetHardwareInfoUtils
import `in`.okcredit.ui._utils.TrackDeviceInfo
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.work.Configuration
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.amp.CTPushAmpListener
import com.github.anrwatchdog.ANRWatchDog
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.instacart.library.truetime.TrueTimeRx
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.uber.rxdogtag.RxDogTag
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import merchant.android.okstream.contract.OkStreamService
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Unauthorized
import tech.okcredit.android.base.AppVariable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.workmanager.AppWorkerFactory
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.PROMOTIONAL_NOTIFICATION_CHANNEL
import tech.okcredit.android.communication.TRANSACTIONS_NOTIFICATION_CHANNEL
import tech.okcredit.android.communication.analytics.CommunicationTracker
import tech.okcredit.app_contract.AppShortcutAdder
import tech.okcredit.base.exceptions.ExceptionUtils.Companion.logRxUnHandleError
import tech.okcredit.home.ui.payables_onboarding.helpers.TabOrderingHelper
import tech.okcredit.secure_keys.KeyProvider
import timber.log.Timber
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

// app extending SplitCompatApplication to support DFM
open class App :
    SplitCompatApplication(),
    HasAndroidInjector,
    LifecycleObserver,
    Configuration.Provider,
    CTPushAmpListener,
    AppComponentProvider {

    @Inject
    lateinit var appWorkerFactory: Lazy<AppWorkerFactory>

    @Inject
    lateinit var authService: Lazy<AuthService>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var communicationTracker: Lazy<CommunicationTracker>

    @Inject
    lateinit var appLockManager: Lazy<AppLockManager>

    @Inject
    lateinit var appVariable: Lazy<AppVariable>

    @Inject
    lateinit var fileUtils: Lazy<FileUtils>

    @Inject
    lateinit var serverConfigManager: Lazy<ServerConfigManager>

    @Inject
    lateinit var analyticsNotificationHelper: Lazy<AnalyticsNotificationHelper>

    @Inject
    lateinit var cleverTapAPI: Lazy<CleverTapAPI>

    @Inject
    lateinit var mixpanelApi: Lazy<MixpanelAPI>

    @Inject
    lateinit var firebaseAnalytics: Lazy<FirebaseAnalytics>

    @Inject
    lateinit var deviceApi: Lazy<DeviceRepository>

    @Inject
    lateinit var ab: Lazy<AbRepository>

    @Inject
    lateinit var getHardwareInfoUtils: Lazy<GetHardwareInfoUtils>

    @Inject
    lateinit var trackDeviceInfo: Lazy<TrackDeviceInfo>

    @Inject
    lateinit var appsFlyerApi: Lazy<AppsFlyerLib>

    @Inject
    lateinit var appFlyerProperties: Lazy<AppsFlyerProperties>

    @Inject
    lateinit var communicationApi: Lazy<CommunicationRepository>

    @Inject
    lateinit var activityLifeCycleCallBacks: Lazy<OkCreditActivityLifeCycleCallBacks>

    @Inject
    lateinit var appStartMeasureLifeCycleCallBacks: Lazy<AppStartMeasureLifeCycleCallBacks>

    @Inject
    lateinit var anrWatchDog: Lazy<ANRWatchDog>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferencesImpl>

    @Inject
    lateinit var appAnalytics: Lazy<AppAnalytics>

    @Inject
    lateinit var appsFlyerHelper: Lazy<AppsFlyerHelper>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var appShortcutAdder: Lazy<AppShortcutAdder>

    @Inject
    lateinit var firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>

    @Inject
    lateinit var analyticsProvider: Lazy<IAnalyticsProvider>

    @Inject
    lateinit var tabOrderingHelper: Lazy<TabOrderingHelper>

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var okStreamService: OkStreamService

    @Inject
    lateinit var anrDebugger: Lazy<ANRDebugger>

    init {
        System.loadLibrary("native-lib")
    }

    companion object {

        private const val APP_LOCK_SESSION_TIME_IN_MINUTES_KEY = "app_lock_session_time_in_minutes"
        private const val ANR_DURATION_KEY = "anr_duration" // 4 seconds
    }

    lateinit var appComponent: AppComponent

    private var disposable: Disposable? = null

    internal var attributionCallbackTime = 0L

    override fun onCreate() {

        StartUpMeasurementDataObject.appOnCreateTime = SystemClock.uptimeMillis()

        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            return
        }

        super.onCreate()
        attributionCallbackTime = System.currentTimeMillis()

        debug {
            setupLogging()
        }

        StartUpMeasurementDataObject.daggerGraphCreationTime = AppStartUpMeasurementUtils.measureTimeDiffInMillis {
            setupDependencyInjection()
        }

        setRxErrorHandler()
        registerActivityLifecycleCallbacks(activityLifeCycleCallBacks.get())

        Executors.newSingleThreadExecutor().execute {
            disableLogging()
            firebaseRemoteConfig.get().fetchAndActivate()
            setUpANRwatchDog()
            syncExperiment()
            appVariable.get().appCreated = true
            setupAppsFlyer()
            setupInstallReferrerTracking()
            setupJobScheduler()
            setupActivityLifecycle()
            createNotificationChannels()
            setupNotificationChannelForClevertap()
            setupCleverTapPushListener()
            setupAnalytics()
            setupTrueTime()
            setupRxDogTag()
            setupAndroidShortcuts()
            setupDeviceHardwareInfoListener()
            trackDeviceInfo()
            fileUtils.get().deleteReminderImages()
        }

        // Needs to be on Main thread
        observeProcessLifecycle()
        setupWebViewDebugging()

        if (StartUpMeasurementDataObject.traceStartUpEnabled) {
            registerActivityLifecycleCallbacks(appStartMeasureLifeCycleCallBacks.get())
            StartUpMeasurementDataObject.setProcessInfo()
            StartUpMeasurementDataObject.appOnCreateEndTime = SystemClock.uptimeMillis()
        }
    }

    private fun setupWebViewDebugging() {
        debug {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    private fun syncExperiment() {
        if (CommonUtils.isAppForegrounded().not()) {
            return
        }

        deviceApi.get().isDeviceReady()
            .filter { it }
            .flatMapCompletable {
                ab.get().sync(null, "app_open")
            }.subscribe()
    }

    private fun setupAppsFlyer() {
        if (CommonUtils.isAppForegrounded().not()) {
            return
        }

        appFlyerProperties.get().set(AppsFlyerProperties.DISABLE_KEYSTORE, true)

        appsFlyerApi.get().setDebugLog(BuildConfig.DEBUG)

        val conversionDataListener = object : AppsFlyerConversionListener {

            override fun onConversionDataSuccess(data: Map<String, Any?>) {
                GlobalScope.launch {
                    // To be called with Raw data from Appsflyer
                    appsFlyerHelper.get().setPreProcessedAppsflyerData(data)

                    val appsFlyerData = appsFlyerHelper.get().toAppsFlyerData(data)
                    val callbackDiff = getAttributionCallbackDiff()

                    appAnalytics.get().trackAppsFlyerAcquisition(callbackDiff, appsFlyerData)
                    tabOrderingHelper.get().setSuspectUserIsSupplier(appsFlyerData)
                    deviceApi.get().addReferrer(Referrer(ReferrerSource.APPS_FLYER.value, data.toString()))
                }
            }

            override fun onConversionDataFail(error: String?) {
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
            }

            override fun onAttributionFailure(error: String?) {
            }
        }

        appsFlyerApi.get().init(KeyProvider.getAppsFlyerKey(), conversionDataListener, applicationContext)
        appsFlyerApi.get().setCustomerUserId(deviceApi.get().deviceDeprecated.id)
        appsFlyerApi.get().startTracking(this)
    }

    /********************** setUp ANR watchDog *********************/
    private fun setUpANRwatchDog() {
        anrWatchDog.get().setANRListener { error ->
            anrDebugger.get().logWorkersAndSendEvent(error)
            Timber.d("<<<< ANR-Watchdog Detected Application Not Responding!")
            RecordException.recordException(error)
            Timber.d("<<<< ANR-Watchdog Error was successfully serialized $error")
        }.setANRInterceptor { duration ->
            val anrDuration = firebaseRemoteConfig.get().getLong(ANR_DURATION_KEY).toInt()
            val ret: Long = anrDuration - duration
            if (ret > 0)
                Timber.d("<<<< ANR-Watchdog Intercepted ANR that is too short ($duration ms), postponing for $ret ms.")
            ret
        }
        anrWatchDog.get().setReportMainThreadOnly().start()
    }

    /****************************************************************
     * Rx Error handling
     *****************************************************************/
    private fun setRxErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            tracker.get().trackRxUnHandledError("Global", throwable)
            logRxUnHandleError(Exception(throwable))
            if (throwable is Unauthorized || throwable.cause != null && throwable.cause is Unauthorized) {
                Timber.v("<<<<NumberChange setRxErrorHandler Unauthorized %s", throwable.message)
            } else if (throwable is UndeliverableException) {
                tracker.get().trackRxUnHandledError("UndeliverableException", throwable)
            } else if (throwable is OnErrorNotImplementedException) {
                tracker.get().trackRxUnHandledError("OnErrorNotImplementedException", throwable)
            } else if (throwable is IllegalArgumentException) {
                tracker.get().trackRxUnHandledError("IllegalArgumentException", throwable)
            } else if (throwable is CompositeException) { // Avoiding CompositeException happening inside TrueTime // https://console.firebase.google.com/u/0/project/okcredit-6cb68/crashlytics/app/android:in.okcredit.merchant/issues/819a838d5956953f595a9c48941e8454?time=last-ninety-days&sessionId=5DF8029603B1000144DF0DDD60166063_DNE_0_v2  // https://github.com/instacart/truetime-android/issues/119
                tracker.get().trackRxUnHandledError("CompositeException", throwable)
            } else {
                tracker.get().trackRxUnHandledError("RuntimeException", throwable)
                throw RuntimeException(throwable)
            }
        }
    }

    /****************************************************************
     * Setup App Shortcut *
     ****************************************************************/

    private fun setupAndroidShortcuts() {
        if (authService.get().isAuthenticated() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                appShortcutAdder.get()
                    .addAppShortcutIfNotAdded(AppShortcutAdder.Shortcut.ADD_CUSTOMER, this)
                appShortcutAdder.get().addAppShortcutIfNotAdded(
                    AppShortcutAdder.Shortcut.SEARCH_CUSTOMER,
                    this
                )
                appShortcutAdder.get().addAppShortcutIfNotAdded(
                    AppShortcutAdder.Shortcut.ADD_TRANSACTION,
                    this
                )
            } catch (e: Exception) {
                RecordException.recordException(e)
            }
        }
    }

    /****************************************************************
     * Setup MixPanel *
     ****************************************************************/

    /****************************************************************
     * Rx *
     ****************************************************************/

    private fun setupRxDogTag() {
        RxDogTag.install()
    }

    /****************************************************************
     * TrueTime *
     ****************************************************************/
    private fun setupTrueTime() {
        TrueTimeRx.build().initializeRx("time.apple.com")
            .subscribeOn(Schedulers.io())
            .subscribe(
                { date -> Timber.d("TrueTime Latest Time $date") },
                { throwable -> RecordException.recordException(Exception(throwable)) }
            )
    }

    /****************************************************************
     * Device Hardware Information *
     ****************************************************************/
    private fun setupDeviceHardwareInfoListener() {
        if (CommonUtils.isAppForegrounded().not()) {
            return
        }
        try {
            getHardwareInfoUtils.get().execute()
        } catch (e: Exception) {
            Timber.e("Failed to send Device Information for mixpanel.get() ${e.message}")
            RecordException.recordException(e)
        }
    }

    /****************************************************************
     * Device Information *
     ****************************************************************/
    private fun trackDeviceInfo() {
        if (CommonUtils.isAppForegrounded().not()) {
            return
        }
        try {
            trackDeviceInfo.get().execute()
        } catch (e: Exception) {
            Timber.e("Failed to send Device Information for mixpanel.get() ${e.message}")
            RecordException.recordException(e)
        }
    }

    /****************************************************************
     * Job Scheduling
     ****************************************************************/

    private fun setupJobScheduler() {
        if (CommonUtils.isAppForegrounded().not()) {
            return
        }

        serverConfigManager.get().schedule()
    }

    private fun setupAnalytics() {
        val analyticsProviders: MutableSet<AnalyticsHelper> = HashSet()
        analyticsProviders.add(MixpanelAnalyticsProvider(mixpanelApi.get()))
        analyticsProviders.add(CrashlyticsAnalyticsHelper())
        analyticsProviders.add(ClevertapAnalyticsProvider(cleverTapAPI.get(), localeManager))
        analyticsProviders.add(FirebaseAnalyticsProvider(firebaseAnalytics.get()))
        analyticsProviders.add(AppsFlyerAnalyticsProvider(this, appsFlyerApi.get()))
        debug {
            analyticsProviders.add(DebugAnalyticsProvider(analyticsNotificationHelper.get()))
        }
        Analytics.setup(analyticsProviders)
        if (!appsFlyerApi.get().getOutOfStore(this).isNullOrBlank()) {
            tracker.get().setUserProperty(UserProperties.INSTALL_SOURCE, appsFlyerApi.get().getOutOfStore(this))
        }

        val sessionId = UUID.randomUUID().toString()
        analyticsProvider.get().setSuperProperties(
            mapOf(
                AppAnalytics.Key.GENERATED_BY to AppAnalytics.Value.ANDROID_APP,
                AppAnalytics.Key.SESSION_ID to sessionId,
            )
        )
    }

    /****************************************************************
     * Notification
     ****************************************************************/

    private fun createNotificationChannels() {
        nougat {
            communicationApi.get().createNotificationChannel(PROMOTIONAL_NOTIFICATION_CHANNEL)
            communicationApi.get().createNotificationChannel(TRANSACTIONS_NOTIFICATION_CHANNEL)
        }
    }

    private fun setupNotificationChannelForClevertap() {
        nougat {
            CleverTapAPI.createNotificationChannel(
                this,
                PROMOTIONAL_NOTIFICATION_CHANNEL.channelId,
                PROMOTIONAL_NOTIFICATION_CHANNEL.name,
                PROMOTIONAL_NOTIFICATION_CHANNEL.descriptionText,
                NotificationManager.IMPORTANCE_MAX,
                true
            )
        }
    }

    private fun setupCleverTapPushListener() {
        cleverTapAPI.get().ctPushAmpListener = this
    }

    /****************************************************************
     * Lifecycle
     ****************************************************************/
    private fun setupActivityLifecycle() {
        ActivityLifecycleCallback.register(this)
    }

    /****************************************************************
     * Logging
     ****************************************************************/
    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugLoggingTree())
        } else {
            Timber.plant(ProdLoggingTree())
        }
    }

    private fun disableLogging() {
        if (`in`.okcredit.analytics.BuildConfig.DEBUG) {
            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.INFO)
        } else {
            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.OFF)
        }
    }

    /****************************************************************
     * JodaTime
     *****************************************************************/

    private fun setupJodaTimeAndroid() {
        /*
        Commenting this method reason -: removed net.danlew:android.joda:$versions.jodaTime so this method will not work
         Removal of net.danlew:android.joda:$versions.jodaTime is done because it didnt worked properly with time zone
         reverting to joda-time:joda-time:$versions.jodaTime, still exact due date fix is not found as the lib change was
         done around 1  but previously it was working
         but reverting lib was helpful so keeping in this state for now
          */
//        JodaTimeAndroid.init(this)
    }

    /****************************************************************
     * Context Wrappers
     ****************************************************************/
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.setLocale(base))
        MultiDex.install(this)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setLocale(this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setExecutor(createWorkerExecutor())
            .setWorkerFactory(appWorkerFactory.get())
            .setMinimumLoggingLevel(Log.ASSERT)
            .build()
    }

    private fun createWorkerExecutor(): Executor {
        return Executors.newFixedThreadPool( // Min-2, Max-8
            max(2, min(Runtime.getRuntime().availableProcessors() - 1, 8))
        )
    }

    private fun setupDependencyInjection() {
        appComponent = DaggerAppComponent
            .builder()
            .app(this)
            .build()
        appComponent.inject(this)
    }

    /****************************************************************
     * Install Referrer Tracking (Play Store)
     *****************************************************************/
    private fun setupInstallReferrerTracking() {
        if (CommonUtils.isAppForegrounded().not()) {
            return
        }
        try {
            val referrerClient = InstallReferrerClient.newBuilder(this).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                referrerClient.installReferrer.installReferrer?.let {
                                    appAnalytics.get()
                                        .trackInstallRefererAcquisition(
                                            getAttributionCallbackDiff(),
                                            appsFlyerHelper.get().getInstallRefererData(it)
                                        )
                                    deviceApi.get().addReferrer(Referrer(ReferrerSource.PLAY_STORE.value, it))
                                }
                                referrerClient.endConnection()
                            } catch (e: RemoteException) {
                                Timber.e(e)
                            }
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() { // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
                }
            })
        } catch (ignored: Exception) {
        }
    }

    // This will show authentication(lock) screen before entering in to the app
    // when the app is in background for 20 min Or more (resume session time), and user opens the app , he as to authenticate to enter the app
    private fun observeProcessLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onAppForeground() {
                okStreamService.connect(this@App)
                disposable?.dispose()
            }

            // TODO : fix IO operations done on main thread
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onAppBackground() {
                val customAppLockActive: Boolean = appLockManager.get().isAppLockActive()
                val systemSecurityEnabled = onboardingPreferences.get().isAppLockEnabled()
                if (customAppLockActive || systemSecurityEnabled) {
                    val appLockSessionTimeInMinutes = firebaseRemoteConfig.get().getLong(
                        APP_LOCK_SESSION_TIME_IN_MINUTES_KEY
                    ).toInt()
                    disposable = Completable.timer(appLockSessionTimeInMinutes.toLong(), TimeUnit.MINUTES)
                        .subscribe {
                            onAppBackground()
                            onboardingPreferences.get().setAppWasInBackgroundFor20Minutes(true)
                        }
                }

                okStreamService.disconnect(this@App)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onAppKilled() {

                Timber.i("OnLifecycleEvent ON_DESTROY")
            }
        })
    }

    override fun onPushAmpPayloadReceived(extras: Bundle?) {
        Timber.i("Push Amplification: PayLoad Received")
        try {
            CleverTapAPI.createNotification(applicationContext, extras)
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private fun getAttributionCallbackDiff(): Double {
        val timeDifference = ((System.currentTimeMillis() - attributionCallbackTime))
        return timeDifference / 1000.0
    }

    override fun provideAppComponent(): AppComponent {
        return appComponent
    }
}
