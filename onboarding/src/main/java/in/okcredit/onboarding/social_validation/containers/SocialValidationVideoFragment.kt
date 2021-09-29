package `in`.okcredit.onboarding.social_validation.containers

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.databinding.FragmentVideoBackedPageBinding
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class SocialValidationVideoFragment : Fragment(R.layout.fragment_video_backed_page) {

    companion object {

        private const val URL = "url"
        private const val SUBTITLE = "subtitle"

        fun newInstance(page: SocialValidationPage.VideoBacked): SocialValidationVideoFragment {
            val fragment = SocialValidationVideoFragment()
            fragment.arguments = Bundle().also {
                it.putString(URL, page.url)
                it.putString(SUBTITLE, page.subtitle)
            }
            return fragment
        }
    }

    private val binding: FragmentVideoBackedPageBinding by viewLifecycleScoped(FragmentVideoBackedPageBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
