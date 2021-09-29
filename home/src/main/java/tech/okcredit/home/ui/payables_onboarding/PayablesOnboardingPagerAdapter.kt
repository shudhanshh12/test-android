package tech.okcredit.home.ui.payables_onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import tech.okcredit.home.R
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingPage.ImageBacked
import tech.okcredit.home.ui.payables_onboarding.PayablesOnboardingPage.VideoBacked
import tech.okcredit.home.ui.payables_onboarding.fragments.ImagedBackedCarouselFragment
import tech.okcredit.home.ui.payables_onboarding.video_backed_carousel_fragment.VideoBackedCarouselFragment

class PayablesOnboardingPagerAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {

    companion object {
        val content: List<PayablesOnboardingPage> = listOf(
            ImageBacked(R.drawable.ic_supplier_carousels_01, R.string.payables_onboarding_carousel_string_1),
            ImageBacked(R.drawable.ic_supplier_carousels_02, R.string.payables_onboarding_carousel_string_2),
            ImageBacked(R.drawable.ic_supplier_carousels_03, R.string.payables_onboarding_carousel_string_3),
            VideoBacked("rDA3Dwp18dw", R.string.payables_onboarding_carousel_string_video)
        )
    }

    override fun getItemCount(): Int {
        return content.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (val page = content[position]) {
            is ImageBacked -> ImagedBackedCarouselFragment.newInstance(page.res, page.string)
            is VideoBacked -> VideoBackedCarouselFragment.newInstance(
                page.youtubeId, page.string
            )
        }
    }
}
