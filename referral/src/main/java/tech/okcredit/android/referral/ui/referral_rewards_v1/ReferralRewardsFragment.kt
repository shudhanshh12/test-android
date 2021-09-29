package tech.okcredit.android.referral.ui.referral_rewards_v1

import `in`.okcredit.rewards.contract.RewardsNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.referred_merchant_list.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.Intent
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.State
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.ViewEvent
import tech.okcredit.android.referral.rewards.ui.ReferralRewardsContract.ViewEvent.DeferredScrollToPos
import javax.inject.Inject

class ReferralRewardsFragment : BaseFragment<State, ViewEvent, Intent>(
    "ReferredMerchantListScreen",
    R.layout.referred_merchant_list
) {

    @Inject
    lateinit var merchantListController: Lazy<ReferralRewardsController>

    @Inject
    lateinit var rewardsNavigator: Lazy<RewardsNavigator>

    @Inject
    lateinit var eventTracker: Lazy<ReferralEventTracker>

    val scrollToPos = CompletableDeferred<Int>()
    lateinit var scrollToPosJob: Job

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with((merchantsRecyclerView as EpoxyRecyclerView)) {
            layoutManager = LinearLayoutManager(context)
            adapter = merchantListController.get().adapter

            merchantListController.get().apply {
                onNotifyClicked = ::onNotifyClicked
                onEarnMoreClicked = ::onEarnMoreClicked

                addModelBuildListener {
                    if (!this@ReferralRewardsFragment::scrollToPosJob.isInitialized) {
                        scrollToPosJob = lifecycleScope.launchWhenCreated {
                            scrollToPos.await()
                                .takeUnless { it < 0 }
                                ?.also { pos -> this@with.scrollToPosition(pos) }
                        }
                    }
                }
            }
        }

        unclaimedRewardsButton.setOnClickListener {
            eventTracker.get().trackUnclaimedRewardsInteracted(getCurrentState().referralVersion)
            rewardsNavigator.get().goToRewardsScreen(requireContext())
        }

        toolbar.setNavigationOnClickListener { this.findNavController().popBackStack() }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            retryButton.clicks().map { Intent.Load }
        )
    }

    override fun render(state: State) {
        if (state.showProgressBar) {
            progressbar.visible()
        } else {
            progressbar.gone()
        }

        if (state.toastMessage != null) {
            shortToast(getString(state.toastMessage!!))
        }

        if (state.referredMerchants.isNotEmpty()) {
            merchantListController.get().setMerchants(state.referredMerchants)
        }

        earningAmountTextView.text = getString(R.string.rupee_placeholder, state.earnings)
        if (state.unclaimedRewards.isNullOrBlank()) {
            unclaimedRewardsButton.gone()
        } else {
            unclaimedRewardsButton.text = getString(R.string.unclaimed_rewards, state.unclaimedRewards)
            unclaimedRewardsButton.visible()
        }

        if (state.showError) {
            state.errorMessage?.let {
                errorTextView.setText(it)
            }
            errorViews.visible()
            successViews.gone()
        } else {
            errorViews.gone()
            successViews.visible()
        }
    }

    private fun onNotifyClicked(phoneNumber: String) {
        eventTracker.get().trackNotifyMerchantInteracted(getCurrentState().referralVersion)
        pushIntent(Intent.Notify(phoneNumber))
    }

    private fun onEarnMoreClicked() {
        eventTracker.get().trackEarnMoreRewardsInteracted(getCurrentState().referralVersion)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is DeferredScrollToPos -> scrollToPos.complete(event.pos)
        }
    }
}
