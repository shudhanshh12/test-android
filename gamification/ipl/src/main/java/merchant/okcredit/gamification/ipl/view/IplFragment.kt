package merchant.okcredit.gamification.ipl.view

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplFragmentBinding
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.view.IplContract.*
import tech.okcredit.android.base.extensions.dpToPixel
import tech.okcredit.android.base.extensions.getDisplayMatrix
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.OuterCirclePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import javax.inject.Inject

class IplFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "IplScreen",
        R.layout.ipl_fragment
    ),
    ViewPager.OnPageChangeListener {

    private val binding: IplFragmentBinding by viewLifecycleScoped(IplFragmentBinding::bind)

    @Inject
    lateinit var tabAdapter: Lazy<IplViewPagerAdapter>

    @Inject
    lateinit var eventTracker: Lazy<IplEventTracker>

    private var todayToolTip: MaterialTapTargetPrompt? = null
    private var weeklyToolTip: MaterialTapTargetPrompt? = null
    private var leaderBoradToolTip: MaterialTapTargetPrompt? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    internal fun init() {
        binding.viewPager.apply {
            adapter = tabAdapter.get()
            offscreenPageLimit = 3
            addOnPageChangeListener(this@IplFragment)
        }
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        when (activity?.intent?.action) {
            IplActivity.ACTION_WEEKLY_DRAW -> binding.viewPager.setCurrentItem(1, false)
            IplActivity.ACTION_LEADERBOARD -> binding.viewPager.setCurrentItem(2, false)
            else -> eventTracker.get().matchScreenClicked()
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {}

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.ShowEducation -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(dispatcherProvider.get().main()) {
                        delay(GAME_TUTORIAL_DELAY)
                        showTodaysGameEducation()
                    }
                }
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {
            0 -> eventTracker.get().matchScreenClicked()
            1 -> eventTracker.get().weeklyDrawClicked()
            2 -> eventTracker.get().leaderBoardClicked()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun showTodaysGameEducation() {
        val todaysTab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(0)
        val width = getDisplayMatrix()
        val outerRadius = width - width / 4
        val radius = dpToPixel(48f)
        val padding = 0f
        if (todayToolTip != null) return
        todayToolTip = MaterialTapTargetPrompt.Builder(requireActivity())
            .setTarget(todaysTab)
            .setFocalPadding(20f)
            .setBackgroundColour(ContextCompat.getColor(requireContext(), R.color.primary))
            .setPromptBackground(OuterCirclePromptBackground(outerRadius))
            .setPrimaryText(getString(R.string.todays_tab_tool_tip))
            .setPrimaryTextGravity(GravityCompat.START)
            .setPrimaryTextSize(R.dimen.text_size_18)
            .setPromptFocal(RectanglePromptFocal().setCornerRadius(radius, radius).setTargetPadding(padding))
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { prompt, state ->
                when (state) {
                    MaterialTapTargetPrompt.STATE_FINISHED -> showLeaderBoardEducation()
                    MaterialTapTargetPrompt.STATE_DISMISSED -> showLeaderBoardEducation()
                }
            }
            .show()
    }

    private fun showLeaderBoardEducation() {
        val weeklyTab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(1)
        val width = getDisplayMatrix()
        val outerRadius = width - width / 3
        val radius = dpToPixel(48f)
        val padding = 0f
        if (weeklyToolTip != null) return
        weeklyToolTip = MaterialTapTargetPrompt.Builder(requireActivity())
            .setTarget(weeklyTab)
            .setFocalPadding(20f)
            .setBackgroundColour(ContextCompat.getColor(requireContext(), R.color.primary))
            .setPromptBackground(OuterCirclePromptBackground(outerRadius))
            .setPrimaryText(getString(R.string.leaderboard_tool_tip))
            .setPrimaryTextSize(R.dimen.text_size_18)
            .setPrimaryTextGravity(GravityCompat.START)
            .setPromptFocal(RectanglePromptFocal().setCornerRadius(radius, radius).setTargetPadding(padding))
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                when (state) {
                    MaterialTapTargetPrompt.STATE_FINISHED -> showWeeklyGameEducation()
                    MaterialTapTargetPrompt.STATE_DISMISSED -> showWeeklyGameEducation()
                }
            }
            .show()
    }

    private fun showWeeklyGameEducation() {
        val leaderboardTab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(2)
        val width = getDisplayMatrix()
        val radius = dpToPixel(48f)
        val padding = 0f
        if (leaderBoradToolTip != null) return
        leaderBoradToolTip =
            MaterialTapTargetPrompt.Builder(requireActivity())
                .setTarget(leaderboardTab)
                .setFocalPadding(20f)
                .setBackgroundColour(ContextCompat.getColor(requireContext(), R.color.primary))
                .setPromptBackground(OuterCirclePromptBackground(width))
                .setPrimaryText(getString(R.string.weekly_tool_tip))
                .setPrimaryTextSize(R.dimen.text_size_18)
                .setPrimaryTextGravity(GravityCompat.START)
                .setPromptFocal(
                    RectanglePromptFocal().setCornerRadius(radius, radius).setTargetPadding(padding)
                )
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener { _, state ->
                    when (state) {
                        MaterialTapTargetPrompt.STATE_FINISHED -> pushIntent(Intent.EducationViewed)
                        MaterialTapTargetPrompt.STATE_DISMISSED -> pushIntent(Intent.EducationViewed)
                    }
                }
                .show()
    }

    companion object {
        const val GAME_TUTORIAL_DELAY = 2000L
    }
}
