package merchant.okcredit.gamification.ipl.rewards.mysteryprize

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ClaimMysteryPrizeFragmentBinding
import merchant.okcredit.gamification.ipl.rewards.mysteryprize.ClaimMysteryPrizeContract.*
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible

class ClaimMysteryPrizeFragment : BaseFragment<State, ViewEvent, Intent>(
    "ClaimMysteryPrizeScreen",
    R.layout.claim_mystery_prize_fragment
) {

    companion object {

        fun newInstance() = ClaimMysteryPrizeFragment()
    }

    private val binding: ClaimMysteryPrizeFragmentBinding by viewLifecycleScoped(ClaimMysteryPrizeFragmentBinding::bind)

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layout.setOnClickListener { requireActivity().finish() }
    }

    override fun render(state: State) {
        if (state.welcomeReward) {
            binding.apply {
                mysteryPrize.points.text = getString(R.string.runs_placeholder, state.amount.toString())
                mysteryPrize.title.text = getString(R.string.welcome_gift)
                mysteryPrize.icon.setImageResource(R.drawable.ic_werlcome_reward)
            }
        } else {
            binding.apply {
                mysteryPrize.points.text = getString(R.string.points_placeholder, state.amount.toString())
                mysteryPrize.title.text = getString(R.string.you_have_won)
                mysteryPrize.icon.setImageResource(R.drawable.ic_success_trophy)
            }
        }

        if (state.claimInProgress) {
            binding.progressViews.visible()
        } else {
            binding.progressViews.gone()
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.RewardWon -> shortToast(R.string.congratulations)
            is ViewEvent.InternetIssue -> {
                shortToast(R.string.no_internet_msg)
                requireActivity().finish()
            }
            is ViewEvent.ServerError -> {
                shortToast(R.string.reward_claim_failure)
                requireActivity().finish()
            }
        }
    }
}
