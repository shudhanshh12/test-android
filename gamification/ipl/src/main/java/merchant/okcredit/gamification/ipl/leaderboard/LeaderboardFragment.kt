package merchant.okcredit.gamification.ipl.leaderboard

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.LeaderboardFragmentBinding
import merchant.okcredit.gamification.ipl.game.ui.GameRulesCardLeaderboard
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.*
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.Intent.CollapseGameCard
import merchant.okcredit.gamification.ipl.leaderboard.LeaderboardContract.Intent.Load
import merchant.okcredit.gamification.ipl.leaderboard.epoxy.controller.LeaderBoardController
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.setHtmlText
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import javax.inject.Inject

class LeaderboardFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "LeaderboardScreen",
        R.layout.leaderboard_fragment
    ),
    GameRulesCardLeaderboard.OnGameRulesListener {

    @Inject
    lateinit var leaderBoardController: Lazy<LeaderBoardController>

    @Inject
    lateinit var iplEventTracker: Lazy<IplEventTracker>

    private val binding: LeaderboardFragmentBinding by viewLifecycleScoped(LeaderboardFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.apply {
            clPaging.setOnClickListener {
                pushIntent(Intent.Paginate)
            }
            inError.mbRetry.setOnClickListener {
                pushIntent(Intent.Retry)
            }
        }

        binding.rvLeaderBoard.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = leaderBoardController.get().adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val isScrollDown = dy > 0
                    if (isScrollDown) {
                        pushIntent(CollapseGameCard)

                        if (getCurrentState().hasMoreData && getCurrentState().isPaginating.not()) {
                            val totalListSize = leaderBoardController.get().adapter.copyOfModels.size
                            val layout = recyclerView.layoutManager as LinearLayoutManager

                            val lastVisibleItem = layout.findLastCompletelyVisibleItemPosition()

                            val percentage = (lastVisibleItem * 100) / totalListSize
                            if (percentage == PAGINATION_LIMIT_PERCENTAGE) {
                                pushIntent(Intent.Paginate)
                            }
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                }
            })
        }
    }

    override fun loadIntent(): UserIntent {
        return Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        leaderBoardController.get().setData(state.leaderBoardList)
        renderLoading(state)
        renderPaginationState(state)
        renderRulesCollapsed(state)
    }

    private fun renderRulesCollapsed(state: State) {
        if (state.gameRulesCollapsed) {
            leaderBoardController.get().setRulesCollapsed(state.gameRulesCollapsed)
        }
    }

    private fun renderPaginationState(state: State) {
        if (state.isPaginating) {
            binding.apply {
                pagingLoaderLottie.visible()
                pagingLoaderLottie.playAnimation()
            }
        } else {
            binding.apply {
                pagingLoaderLottie.gone()
                pagingLoaderLottie.cancelAnimation()
            }
        }

        when {
            state.paginationNetworkError -> {
                binding.apply {
                    pagingLoaderLottie.visible()
                    pagingLoaderLottie.cancelAnimation()
                    tvPagingError.visible()
                    tvPagingError.setHtmlText(R.string.network_error)
                }
            }
            state.paginationServerError -> {
                binding.apply {
                    pagingLoaderLottie.visible()
                    pagingLoaderLottie.cancelAnimation()
                    tvPagingError.visible()
                    tvPagingError.setHtmlText(R.string.server_error)
                }
            }
            else -> {
                binding.tvPagingError.gone()
            }
        }
    }

    private fun renderLoading(state: State) {
        binding.apply {
            when {
                state.isLoading -> {
                    rvLeaderBoard.gone()
                    shimmerLayout.visible()
                    inError.root.gone()
                }
                state.networkError -> {
                    shimmerLayout.gone()
                    rvLeaderBoard.gone()
                    inError.root.visible()
                    inError.apply {
                        tvError.text = getString(R.string.interent_error)
                        ivError.setImageResource(R.drawable.bg_network_error)
                    }
                }
                state.serverError -> {
                    shimmerLayout.gone()
                    rvLeaderBoard.gone()
                    inError.root.visible()
                    inError.apply {
                        tvError.text = getString(R.string.err_default)
                        ivError.setImageResource(R.drawable.bg_server_error)
                    }
                }
                else -> {
                    shimmerLayout.gone()
                    rvLeaderBoard.visible()
                    inError.root.gone()
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
        fun newInstance() = LeaderboardFragment()
        const val PAGINATION_LIMIT_PERCENTAGE = 80
    }

    override fun onGameRuleInteracted() {
        binding.rvLeaderBoard.scrollToPosition(0)
    }

    override fun onGameRuleExpanded() {
        iplEventTracker.get().gameRuleOpened(IplEventTracker.Value.LEADERBOARD_SCREEN)
    }

    override fun onGameRuleCollapsed() {
        iplEventTracker.get().gameRuleClosed(IplEventTracker.Value.LEADERBOARD_SCREEN)
    }
}
