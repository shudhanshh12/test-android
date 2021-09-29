package `in`.okcredit.merchant.rewards.ui.rewards_screen

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.collection.contract.SetUpCollectionDialogListener
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.analytics.RewardsEventTracker
import `in`.okcredit.merchant.rewards.databinding.RewardsFragmentBinding
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class RewardsFragment :
    BaseFragment<RewardsContract.State, RewardsContract.ViewEvent, RewardsContract.Intent>(
        "RewardsScreen",
        R.layout.rewards_fragment
    ) {

    private var alert: Snackbar? = null

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    lateinit var rewardsEventTracker: Lazy<RewardsEventTracker>

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var rewardsController: Lazy<RewardsController>

    private val binding: RewardsFragmentBinding by viewLifecycleScoped(RewardsFragmentBinding::bind)

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    override fun onDestroyView() {
        rewardsController.get().adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    override fun loadIntent(): UserIntent {
        return RewardsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.just(RewardsContract.Intent.OnRefresh)
    }

    override fun render(state: RewardsContract.State) {
        binding.contextualHelp.setContextualHelpIds(state.contextualHelpIds)
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(binding.recyclerView)
        rewardsController.get().setState(state)

        binding.amount.text = String.format("₹%s", formatV2(state.sumOfClaimedRewards))

        if (state.networkError || state.isAlertVisible) {
            alert = if (state.isAlertVisible && state.alertMessage != null) {
                view?.snackbar(getString(state.alertMessage), Snackbar.LENGTH_LONG)
            } else {
                view?.snackbar(getString(R.string.home_no_internet_msg), Snackbar.LENGTH_LONG)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeToRefresh.setOnRefreshListener {
            tracker.trackRefresh(Event.REWARD_SCREEN)
            binding.swipeToRefresh.isRefreshing = false
            pushIntent(RewardsContract.Intent.OnRefresh)
        }

        binding.contextualHelp.initDependencies(
            screenName = ScreenName.RewardsScreen.value,
            tracker = tracker,
            legacyNavigator = legacyNavigator.get()
        )
        binding.recyclerView.apply {
            adapter = rewardsController.get().adapter
            rewardsController.get().adapter.registerAdapterDataObserver(dataObserver)
        }
        rewardsEventTracker.get().trackRewardScreenViewed()
        binding.rootView.setTracker(performanceTracker)
    }

    private fun showCollectionAdoptionDialog(unclaimedReward: Long) {
        rewardsEventTracker.get().trackSetupCollectionViewed()
        collectionNavigator.get()
            .showSetUpCollectionDialog(
                childFragmentManager, unclaimedReward,
                object : SetUpCollectionDialogListener {
                    override fun onSetUpCollectionClick() {
                        collectionNavigator.get().showMerchantDestinationDialog(
                            fragmentManager = childFragmentManager,
                            asyncRequest = true,
                            source = Event.REWARD_SCREEN
                        )
                        tracker.trackEvents(Event.SETUP_COLLECTION_CLICKED, screen = Event.REWARD_CLAIMED_SCREEN)
                        tracker.trackEvents(
                            Event.STARTED_ADOPT_COLLECTION,
                            source = Event.REWARD_SCREEN,
                            propertiesMap = PropertiesMap.create()
                                .add(PropertyKey.REWARD_AMOUNT, unclaimedReward)
                        )
                    }

                    override fun onCancelled() {
                        activity?.finish()
                        tracker.trackEvents(Event.BACK_CLICKED, screen = Event.REWARD_CLAIMED_SCREEN)
                    }
                }
            )
    }

    override fun handleViewEvent(event: RewardsContract.ViewEvent) {
        when (event) {
            is RewardsContract.ViewEvent.ShowAddMerchantDestinationDialog -> showCollectionAdoptionDialog(event.unclaimedRewards)
        }
    }
}
