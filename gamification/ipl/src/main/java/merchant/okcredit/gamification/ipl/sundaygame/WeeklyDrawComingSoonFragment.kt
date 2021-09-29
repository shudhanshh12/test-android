package merchant.okcredit.gamification.ipl.sundaygame

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ComingSoonFragmentBinding
import tech.okcredit.android.base.extensions.setHtmlText
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class WeeklyDrawComingSoonFragment : Fragment(R.layout.coming_soon_fragment) {

    companion object {

        @JvmStatic
        fun newInstance() = WeeklyDrawComingSoonFragment()
    }

    private val binding: ComingSoonFragmentBinding by viewLifecycleScoped(ComingSoonFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvComingSoon.setHtmlText(R.string.coming_soon_weekly_new)
    }
}
