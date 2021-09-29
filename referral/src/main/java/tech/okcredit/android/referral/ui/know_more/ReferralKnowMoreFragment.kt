package tech.okcredit.android.referral.ui.know_more

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.collection.contract.SetUpCollectionDialogListener
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.text.Html
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.navigate
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.databinding.FragmentReferralKnowMoreBinding
import tech.okcredit.android.referral.ui.know_more.ReferralKnowMoreContract.*
import javax.inject.Inject

class ReferralKnowMoreFragment : BaseFragment<State, ViewEvent, Intent>(
    "ReferralKnowMoreFragment",
    R.layout.fragment_referral_know_more
) {

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var referralEventTracker: Lazy<ReferralEventTracker>

    private val binding: FragmentReferralKnowMoreBinding by viewLifecycleScoped(FragmentReferralKnowMoreBinding::bind)

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.totalRewards.clicks().map {
                tracker.get().trackViewRewards(PropertyValue.REFERRAL)
                referralEventTracker.get()
                    .trackReferralScreenInteracted("Total Rewards button", getCurrentState().version)
                Intent.GoToReferralRewards
            }
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToReferralRewards -> navigate(R.id.referredMerchantListScreen)
            is ViewEvent.ShowCollectionDialog -> showCollectionDialog(event.unclaimedRewards)
        }
    }

    private fun showCollectionDialog(unclaimedRewards: Long) {
        collectionNavigator.get()
            .showSetUpCollectionDialog(
                childFragmentManager, unclaimedRewards,
                object : SetUpCollectionDialogListener {
                    override fun onSetUpCollectionClick() {
                        collectionNavigator.get().showMerchantDestinationDialog(
                            fragmentManager = childFragmentManager,
                            asyncRequest = true,
                            source = Event.REWARD_SCREEN
                        )
                        tracker.get().trackEvents(Event.SETUP_COLLECTION_CLICKED, screen = Event.REWARD_CLAIMED_SCREEN)
                        tracker.get().trackEvents(
                            Event.STARTED_ADOPT_COLLECTION,
                            source = Event.REWARD_SCREEN,
                            propertiesMap = PropertiesMap.create()
                                .add(PropertyKey.REWARD_AMOUNT, unclaimedRewards ?: 0L)
                        )
                        referralEventTracker.get()
                            .trackCollectionInteracted("Set Up Collection Button", getCurrentState().version)
                    }

                    override fun onCancelled() {
                        tracker.get().trackEvents(Event.BACK_CLICKED, screen = Event.REWARD_CLAIMED_SCREEN)
                        referralEventTracker.get().trackCollectionDismissed()
                    }
                }
            )
    }

    override fun render(state: State) {
        binding.apply {
            state.referralInfo?.let {
                shareTextWithPrice.text = Html.fromHtml(
                    getString(
                        R.string.share_okcredit_app_and_get_rewards,
                        formatV2(it.maxAmount!!)
                    )
                )
            }

            if (state.totalRewards == 0L) {
                rewardsViews.gone()
            } else {
                totalRewardsAmount.text =
                    getString(R.string.rupee_placeholder, formatV2(state.totalRewards))
                rewardsViews.visible()
            }
        }
    }
}
