package tech.okcredit.android.referral.ui.referral_screen

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import android.os.Bundle
import android.view.View
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.databinding.FragmentReferralBinding
import tech.okcredit.android.referral.ui.referral_screen.ReferralContract.*
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListFragment
import tech.okcredit.android.referral.ui.share.ShareReferralFragment
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.userSupport.SupportRepository
import javax.inject.Inject

class ReferralFragment : BaseFragment<State, ReferralViewEvent, Intent>(
    "ReferralScreen",
    R.layout.fragment_referral
) {

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var userSupport: Lazy<SupportRepository>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private val binding: FragmentReferralBinding by viewLifecycleScoped(FragmentReferralBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.includeAppBar.apply {
            contextualHelp.initDependencies(
                screenName = ScreenName.ShareOkCreditScreen.value,
                tracker = tracker.get(),
                legacyNavigator = legacyNavigator.get()
            )
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun handleViewEvent(event: ReferralViewEvent) {
        when (event) {
            is ReferralViewEvent.ShowTargetedUsersFragment -> replaceFragment(
                ReferralTargetedUsersListFragment(),
                R.id.share_fragment
            )
            is ReferralViewEvent.ShowShareFragment -> replaceFragment(ShareReferralFragment(), R.id.share_fragment)
            is ReferralViewEvent.NoReferralError -> {
                shortToast(R.string.no_reward_error)
                RecordException.recordException(RuntimeException("Growth: Referral: No Referral Reward"))
                requireActivity().finish()
            }
        }
    }

    override fun render(state: State) {
        binding.includeAppBar.contextualHelp.setContextualHelpIds(state.contextualHelpIds)
    }
}
