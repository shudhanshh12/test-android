package tech.okcredit.home.ui.payables_onboarding.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.FragmentImageBackedPageBinding

class ImagedBackedCarouselFragment : Fragment(R.layout.fragment_image_backed_page) {

    companion object {

        private const val DRAWABLE_RES = "drawable_res"
        private const val STRING_RES = "string_res"

        fun newInstance(@DrawableRes res: Int, @StringRes string: Int): ImagedBackedCarouselFragment {
            val fragment = ImagedBackedCarouselFragment()
            fragment.arguments = Bundle().also {
                it.putInt(DRAWABLE_RES, res)
                it.putInt(STRING_RES, string)
            }
            return fragment
        }
    }

    internal val binding: FragmentImageBackedPageBinding by viewLifecycleScoped(FragmentImageBackedPageBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            titleText.setText(arguments?.getInt(STRING_RES) ?: 0)
            imageView.setImageResource(arguments?.getInt(DRAWABLE_RES) ?: 0)
        }
    }
}
