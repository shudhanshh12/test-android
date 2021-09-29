package merchant.okcredit.gamification.ipl.sundaygame

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.SundaygameFragmentBinding
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardWeekly
import merchant.okcredit.gamification.ipl.game.ui.youtube.YoutubeActivity
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract.*
import merchant.okcredit.gamification.ipl.sundaygame.SundayGameContract.Intent.Load
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameController
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import javax.inject.Inject

class SundayGameFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "SundayGameScreen",
        R.layout.sundaygame_fragment
    ),
    GameRulesCardWeekly.OnGameRulesListener {

    @Inject
    lateinit var sundayGameController: Lazy<SundayGameController>

    @Inject
    lateinit var iplEventTracker: Lazy<IplEventTracker>

    private val binding: SundaygameFragmentBinding by viewLifecycleScoped(SundaygameFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.rvPlayGames.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = sundayGameController.get().adapter
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    if (dy < 0) {
//                        pushIntent(CollapseGameCard)
//                    }
//                }
//
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                }
//            })
        }
        binding.inError.mbRetry.setOnClickListener { pushIntent(Intent.Load) }
    }

    override fun loadIntent(): UserIntent {
        return Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        renderLoading(state)
        if (!state.cardList.isNullOrEmpty()) {
            sundayGameController.get().setData(state.cardList)
        }
    }

    private fun renderLoading(state: State) {
        binding.apply {
            when {
                state.isLoading -> {
                    inError.root.gone()
                    gameViews.gone()
                    shimmerLayout.visible()
                }
                state.networkError -> {
                    shimmerLayout.gone()
                    gameViews.gone()
                    inError.root.visible()
                    inError.apply {
                        tvError.text = getString(R.string.interent_error)
                        ivError.setImageResource(R.drawable.bg_network_error)
                    }
                }
                state.serverError -> {
                    shimmerLayout.gone()
                    gameViews.gone()
                    inError.root.visible()
                    inError.apply {
                        tvError.text = getString(R.string.err_default)
                        ivError.setImageResource(R.drawable.bg_server_error)
                    }
                }
                else -> {
                    inError.root.gone()
                    shimmerLayout.gone()
                    gameViews.visible()
                }
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.NetworkErrorToast -> longToast(R.string.interent_error)
            is ViewEvent.ServerErrorToast -> longToast(R.string.err_default)
        }
    }

    companion object {
        fun newInstance() = SundayGameFragment()
    }

    override fun onGameRuleInteracted() {
    }

    override fun onGameRuleExpanded() {
        iplEventTracker.get().gameRuleOpened(IplEventTracker.Value.SUNDAY_TAB_SCREEN)
        getCurrentState().cardList?.size?.let { binding.rvPlayGames.smoothScrollToPosition(it) }
    }

    override fun onGameRuleCollapsed() {
        iplEventTracker.get().gameRuleClosed(IplEventTracker.Value.TODAYS_TAB_SCREEN)
    }

    override fun onHowToPlayOkPl() {
        iplEventTracker.get().youtubeSelected(IplEventTracker.Value.SUNDAY_TAB_SCREEN)
        YoutubeActivity.start(requireActivity(), getCurrentState().youtubeUrl, IplEventTracker.Value.SUNDAY_TAB_SCREEN)
    }
}
