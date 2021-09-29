package `in`.okcredit.onboarding.social_validation.containers

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.databinding.FragmentImageBackedPageBinding
import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class SocialValidationImageFragment : Fragment(R.layout.fragment_image_backed_page) {

    companion object {

        private const val URL = "url"
        private const val DURATION = "duration"

        fun newInstance(page: SocialValidationPage.ImageBacked): SocialValidationImageFragment {
            val fragment = SocialValidationImageFragment()
            fragment.arguments = Bundle().also {
                it.putString(URL, page.url)
                it.putLong(DURATION, page.duration)
            }
            return fragment
        }
    }

    private val binding: FragmentImageBackedPageBinding by viewLifecycleScoped(FragmentImageBackedPageBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(URL)?.also {
            val radius = view.context.resources.getDimensionPixelOffset(R.dimen._16dp)
            GlideApp.with(requireContext())
                .load(it)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
                .into(binding.imageView)
        }
    }
}
