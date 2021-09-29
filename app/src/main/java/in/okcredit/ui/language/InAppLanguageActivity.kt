package `in`.okcredit.ui.language

import `in`.okcredit.R
import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.SetMerchantPreference
import `in`.okcredit.backend.contract.ServerConfigManager
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationRepository
import `in`.okcredit.databinding.ScreenLanguageInAppBinding
import `in`.okcredit.dynamicview.data.repository.DynamicViewRepositoryImpl
import `in`.okcredit.individual.contract.PreferenceKey.LANGUAGE
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.language.usecase.SelectLanguage
import `in`.okcredit.shared.utils.ScreenName
import `in`.okcredit.ui._base_v2.BaseActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.help.help_main.usecase.GetHelp
import tech.okcredit.userSupport.SupportRepository
import timber.log.Timber
import javax.inject.Inject

class InAppLanguageActivity : BaseActivity() {
    private var languagePref: String? = null

    private var context: Context? = null
    private var originValue = ""
    private var setValue = ""

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var serverConfigManager: Lazy<ServerConfigManager>

    @Inject
    lateinit var setMerchantPreference: Lazy<SetMerchantPreference>

    @Inject
    lateinit var businessApi: Lazy<BusinessRepository>

    @Inject
    lateinit var getHelp: Lazy<GetHelp>

    @Inject
    lateinit var dynamicViewRepository: Lazy<DynamicViewRepositoryImpl>

    @Inject
    lateinit var inAppNotificationRepository: Lazy<InAppNotificationRepository>

    @Inject
    lateinit var getActiveBusinessId: Lazy<GetActiveBusinessId>

    private lateinit var binding: ScreenLanguageInAppBinding

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var selectLanguage: Lazy<SelectLanguage>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferences>

    @Inject
    lateinit var userSupport: Lazy<SupportRepository>

    /* Lifecycle methods */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenLanguageInAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Analytics.track(AnalyticsEvents.IN_APP_LANGUAGE_SCREEN)
        showActionBar(true)
        setTitle(R.string.account_language)
        context = this
        binding.contextualHelp.setScreenNameValue(
            ScreenName.LanguageScreen.value, tracker.get(), userSupport.get(),
            legacyNavigator.get()
        )
        binding.englishView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "english")
            )
            proceed(LocaleManager.LANGUAGE_ENGLISH)
        }
        binding.hindiView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "hindi")
            )
            proceed(LocaleManager.LANGUAGE_HINDI)
        }
        binding.punjabiView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "punjabi")
            )
            proceed(LocaleManager.LANGUAGE_PUNJABI)
        }
        binding.malayalamView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "malayalam")
            )
            proceed(LocaleManager.LANGUAGE_MALAYALAM)
        }
        binding.hinglishView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "hinglish")
            )
            proceed(LocaleManager.LANGUAGE_HINGLISH)
        }
        binding.gujaratiView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "gujarati")
            )
            proceed(LocaleManager.LANGUAGE_GUJARATI)
        }
        binding.marathiView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "marathi")
            )
            proceed(LocaleManager.LANGUAGE_MARATHI)
        }
        binding.teluguView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "telugu")
            )
            proceed(LocaleManager.LANGUAGE_TELUGU)
        }
        binding.tamilView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "tamil")
            )
            proceed(LocaleManager.LANGUAGE_TAMIL)
        }
        binding.kannadaView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "kannada")
            )
            proceed(LocaleManager.LANGUAGE_KANNADA)
        }
        binding.bengaliView.setOnClickListener { view: View? ->
            Analytics.track(
                AnalyticsEvents.LANGUAGE_SELECTED_IN_APP,
                EventProperties
                    .create()
                    .with(PropertyKey.TYPE, "bengali")
            )
            proceed(LocaleManager.LANGUAGE_BENGALI)
        }

        binding.rootView.setTracker(performanceTracker)
    }

    override fun onResume() {
        super.onResume()
        setLanguage()
    }

    private fun setLanguage() {
        languagePref = localeManager.get().getLanguage()
        if (languagePref == null) {
            languagePref = LocaleManager.LANGUAGE_ENGLISH
        }
        when (languagePref) {
            LocaleManager.LANGUAGE_ENGLISH -> {
                originValue = "en"
                binding.englishCheck.visible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_HINDI -> {
                originValue = "hi"
                binding.englishCheck.invisible()
                binding.hindiCheck.visible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_PUNJABI -> {
                originValue = "pa"
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.visible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_MALAYALAM -> {
                originValue = "ml"
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.visible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_HINGLISH -> {
                originValue = "afh"
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.visible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_GUJARATI -> {
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.visible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_MARATHI -> {
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.visible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_TELUGU -> {
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.visible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_TAMIL -> {
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.visible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_KANNADA -> {
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.visible()
                binding.bengaliCheck.invisible()
                return
            }
            LocaleManager.LANGUAGE_BENGALI -> {
                binding.englishCheck.invisible()
                binding.hindiCheck.invisible()
                binding.punjabiCheck.invisible()
                binding.malayalamCheck.invisible()
                binding.hinglishCheck.invisible()
                binding.gujaratiCheck.invisible()
                binding.marathiCheck.invisible()
                binding.teluguCheck.invisible()
                binding.tamilCheck.invisible()
                binding.kannadaCheck.invisible()
                binding.bengaliCheck.visible()
                return
            }
            else -> return
        }
    }

    // TODO: HomeClean. All subscription here should move to a Worker.
    fun proceed(language: String) {
        Timber.d("Set Language %s", language)
        setValue = language
        tracker.get().setLangSuperProperty(language)
        tracker.get().trackSelectLanguage(setValue, originValue)
        lifecycleScope.launch(Dispatchers.Default) {
            getActiveBusinessId.get().execute()
                .flatMapCompletable { getHelp.get().scheduleSyncEverything(language, it) }
                .subscribe()
        }
        // TODO : fix IO operation done on main thread
        onboardingPreferences.get().setUserSelectedLanguage(language)
        selectLanguage.get().execute(language).onErrorComplete().subscribe()
        getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            businessApi.get().refreshLanguageInCategories(businessId)
        }.subscribe()

        GlobalScope.launch(CoroutineExceptionHandler { _, _ -> }) {
            val businessId = getActiveBusinessId.get().execute().await()
            dynamicViewRepository.get().syncCustomizations(businessId)
            inAppNotificationRepository.get().scheduleSync(businessId)
        }
        setMerchantPreference.get().execute(LANGUAGE, language).subscribe()
        legacyNavigator.get().goToHome((this))
        finishAffinity()
    }

    companion object {
        fun startingIntent(context: Context): Intent {
            return Intent(context, InAppLanguageActivity::class.java)
        }
    }
}
