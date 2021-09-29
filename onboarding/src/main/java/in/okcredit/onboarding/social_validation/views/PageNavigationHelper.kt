package `in`.okcredit.onboarding.social_validation.views

import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class PageNavigationHelper(
    private val lifecycleOwner: LifecycleOwner,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val viewPager2: ViewPager2,
    private val storyBar: StoryBar,
) {
    private var scrollJob: Job? = null
    private var listener: ViewPager2.OnPageChangeCallback? = null
    private var currentPage: SocialValidationPage? = null

    fun startForData(pages: List<SocialValidationPage>) {
        listener?.also { viewPager2.unregisterOnPageChangeCallback(it) }
        listener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                startForData(pages)
            }
        }.also {
            viewPager2.registerOnPageChangeCallback(it)
        }

        scrollJob?.cancel()
        scrollJob = lifecycleOwner.lifecycleScope.launchWhenStarted {
            with(viewPager2) {
                val page = pages.getOrNull(currentItem) ?: return@launchWhenStarted

                // resume from last progress
                val startFrom = if (currentPage === page) storyBar.percent else 0
                currentPage = page

                when (page) {
                    is SocialValidationPage.ImageBacked -> {
                        val delayTimePerPercent = page.duration / 100
                        (startFrom..99).forEach {
                            storyBar.setProgress(pages.size, currentItem, it)
                            delay(delayTimePerPercent)
                        }
                        currentItem.also { oldPage ->
                            if (goToNextPage()) onboardingAnalytics.trackSocialStoryCompleted(oldPage)
                        }
                    }
                    is SocialValidationPage.VideoBacked -> {
                        storyBar.setProgress(pages.size, currentItem, 0)
                        // todo video
                    }
                }
            }
        }
    }

    fun goToNextPage(): Boolean = with(viewPager2) {
        val count = adapter?.itemCount ?: return@with false
        val currentPos = currentItem // returns 0 for invalid/empty adapter

        if (currentPos <= count - 2) {
            setCurrentItem(currentPos + 1, true)
            true
        } else {
            false
        }
    }

    fun goToPreviousPage(): Boolean = with(viewPager2) {
        val currentPos = currentItem // returns 0 for invalid/empty adapter

        if (currentPos >= 1) {
            setCurrentItem(currentPos - 1, true)
            true
        } else {
            false
        }
    }
}
