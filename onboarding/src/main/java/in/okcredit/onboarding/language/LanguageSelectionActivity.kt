package `in`.okcredit.onboarding.language

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.onboarding.databinding.ActivityLanguageSelectionBinding
import `in`.okcredit.onboarding.language.LanguageSelectionContract.*
import `in`.okcredit.onboarding.language.views.GridItemSpacingDecoration
import `in`.okcredit.onboarding.language.views.LanguageAdapter
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.exhaustive
import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.getLocalisedString
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject
import android.content.Intent as AndroidIntent

class LanguageSelectionActivity : BaseActivity<State, ViewEvent, Intent>(
    "LanguageSelectionActivity"
) {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    private val binding by viewLifecycleScoped(ActivityLanguageSelectionBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUi()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun userIntents(): Observable<UserIntent> = Observable.just(Intent.OnResume)

    private fun initUi() {
        val itemSpacing = resources.getDimension(R.dimen._16dp).toInt()
        binding.rvLanguage.addItemDecoration(GridItemSpacingDecoration(itemSpacing))
    }

    override fun render(state: State) {
        binding.languageLoadingProgress.isVisible = state.isLoading
        binding.rvLanguage.isVisible = !state.isLoading

        if (!state.isLoading && state.languages != null) {
            setLanguages(state.selectedLanguage, state.languages)
        }

        if (state.selectedLanguage.isNotBlank()) {
            binding.selectLanguage.text = getLocalisedString(state.selectedLanguage, R.string.select_your_app_language)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToWelcomeSocialValidation -> legacyNavigator.gotoWelcomeSocialValidationScreen(this)
            is ViewEvent.GoToEnterPhoneNumber -> legacyNavigator.goToEnterMobileScreen(this)
        }.exhaustive
    }

    private fun setLanguages(currentLang: String, languages: List<Language>) {
        binding.rvLanguage.adapter = LanguageAdapter(currentLang, languages, this::onLanguageSelected)
    }

    private fun onLanguageSelected(language: Language) {
        onboardingAnalytics.get().trackSelectLanguage(language.languageCode)
        pushIntent(Intent.LanguageSelected(language.languageCode))
    }

    companion object {

        @JvmStatic
        fun getIntent(context: Context) = AndroidIntent(context, LanguageSelectionActivity::class.java)

        @JvmStatic
        fun start(context: Context) {
            val starter = getIntent(context)
            context.startActivity(starter)
        }
    }
}
