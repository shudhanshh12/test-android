package merchant.okcredit.gamification.ipl.match

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.SelectMatchFragmentBinding
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardNew
import merchant.okcredit.gamification.ipl.game.ui.youtube.YoutubeActivity
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker.Value.TODAYS_TAB_SCREEN
import merchant.okcredit.gamification.ipl.match.SelectMatchContract.*
import merchant.okcredit.gamification.ipl.match.views.MatchLoadErrorView
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class SelectMatchFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "SelectMatchFragment",
        R.layout.select_match_fragment
    ),
    MatchLoadErrorView.RetryListener,
    GameRulesCardNew.OnGameRulesListener {

    companion object {
        fun newInstance() = SelectMatchFragment()
    }

    private val binding: SelectMatchFragmentBinding by viewLifecycleScoped(SelectMatchFragmentBinding::bind)

    @Inject
    lateinit var matchController: Lazy<MatchController>

    @Inject
    lateinit var iplEventTracker: Lazy<IplEventTracker>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.matchRecyclerView.adapter = matchController.get().adapter
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.just(Intent.OnResume)
    }

    override fun render(state: State) {
        matchController.get().setData(state.models)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowTop -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(dispatcherProvider.get().main()) {
                        delay(1500)
                        binding.matchRecyclerView.scrollToPosition(0)
                    }
                }
            }
        }
    }

    override fun retry() {
        pushIntent(Intent.GetActiveMatches)
    }

    override fun onGameRuleInteracted() {
    }

    override fun onGameRuleExpanded() {
        iplEventTracker.get().gameRuleOpened(TODAYS_TAB_SCREEN)
        binding.matchRecyclerView.smoothScrollToPosition(matchController.get().adapter.copyOfModels.size)
    }

    override fun onGameRuleCollapsed() {
        iplEventTracker.get().gameRuleClosed(TODAYS_TAB_SCREEN)
    }

    override fun onHowToPlayOkPl() {
        iplEventTracker.get().youtubeSelected(TODAYS_TAB_SCREEN)
        YoutubeActivity.start(requireActivity(), getCurrentState().youtubeUrl, TODAYS_TAB_SCREEN)
    }
}
