package `in`.okcredit.onboarding.social_validation.containers

import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage.ImageBacked
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage.VideoBacked
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class SocialValidationPagerAdapter(
    fragmentManager: FragmentManager,
    lifeCycle: Lifecycle,
    val data: List<SocialValidationPage>,
) : FragmentStateAdapter(fragmentManager, lifeCycle) {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (val page = data[position]) {
            is ImageBacked -> SocialValidationImageFragment.newInstance(page)
            is VideoBacked -> SocialValidationVideoFragment.newInstance(page)
        }
    }
}
