package `in`.okcredit.onboarding.social_validation

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.databinding.ActivitySocialValidationBinding
import `in`.okcredit.onboarding.social_validation.SocialValidationContract.*
import `in`.okcredit.onboarding.social_validation.containers.SocialValidationPagerAdapter
import `in`.okcredit.onboarding.social_validation.views.PageNavigationHelper
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.exhaustive
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.viewpager2.widget.MarginPageTransformer
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject
import android.content.Intent as AndroidIntent

class SocialValidationActivity : BaseActivity<State, ViewEvent, Intent>(
    "SocialValidationActivity"
) {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    lateinit var pageNavigationHelper: PageNavigationHelper

    private val binding by viewLifecycleScoped(ActivitySocialValidationBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onboardingAnalytics.get().trackViewSocialScreen()

        pageNavigationHelper = PageNavigationHelper(
            this,
            onboardingAnalytics.get(),
            binding.socialValidation,
            binding.storyBar,
        )

        binding.getStarted.setOnClickListener {
            onboardingAnalytics.get().trackGetStartedClicked(
                binding.storyBar.activeIndex,
                binding.storyBar.percent,
            )
            pushIntent(Intent.GetStarted)
        }
        binding.previousStory.setOnClickListener {
            val oldIndex = binding.storyBar.activeIndex
            val oldPercent = binding.storyBar.percent
            if (pageNavigationHelper.goToPreviousPage()) {
                onboardingAnalytics.get().trackSocialPreviousStoryClicked(oldIndex, oldPercent)
            }
        }
        binding.nextStory.setOnClickListener {
            val oldIndex = binding.storyBar.activeIndex
            val oldPercent = binding.storyBar.percent
            if (pageNavigationHelper.goToNextPage()) {
                onboardingAnalytics.get().trackSocialNextStoryClicked(oldIndex, oldPercent)
            }
        }

        // Prevent user from scrolling the view pager, forcing use of tap zones or timer
        binding.socialValidation.isUserInputEnabled = false
        binding.socialValidation.setPageTransformer(MarginPageTransformer(200))

        binding.disclaimer.text = spannableFromHtml(getString(R.string.t_001_login_terms_conditions))
        binding.disclaimer.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun userIntents(): Observable<UserIntent> = Observable.empty()

    override fun render(state: State) {
        binding.loadingProgress.isVisible = state.isLoading

        if (state.pages.isEmpty()) return

        binding.socialValidation.adapter = SocialValidationPagerAdapter(supportFragmentManager, lifecycle, state.pages)

        binding.storyBar.isVisible = state.pages.size > 1
        pageNavigationHelper.startForData(state.pages)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToEnterPhoneNumber -> legacyNavigator.goToEnterMobileScreen(this)
        }.exhaustive
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)

        val clickable: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                span.url?.also {
                    onboardingAnalytics.get().trackDisclaimerClicked(it)
                    legacyNavigator.goToWebViewScreen(this@SocialValidationActivity, it)
                }
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun spannableFromHtml(html: String): SpannableStringBuilder {
        val sequence: CharSequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)

        strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
            .forEach { makeLinkClickable(strBuilder, it) }

        return strBuilder
    }

    companion object {

        @JvmStatic
        fun getIntent(context: Context) = AndroidIntent(context, SocialValidationActivity::class.java)

        @JvmStatic
        fun start(context: Context) {
            val starter = getIntent(context)
            context.startActivity(starter)
        }
    }
}
