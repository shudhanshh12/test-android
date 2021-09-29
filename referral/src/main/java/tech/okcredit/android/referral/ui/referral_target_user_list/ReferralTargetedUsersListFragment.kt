package tech.okcredit.android.referral.ui.referral_target_user_list

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.referral.contract.models.TargetedUser
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.navigate
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.databinding.FragmentTargetedUserListBinding
import tech.okcredit.android.referral.ui.ReferralActivity.Companion.ARG_TARGETED_REFERRAL_PHONE_NUMBER
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.Intent
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.State
import tech.okcredit.android.referral.ui.referral_target_user_list.ReferralTargetedUsersListContract.ViewEvents
import tech.okcredit.android.referral.ui.referral_target_user_list.views.ConvertedTargetedUserItemView
import tech.okcredit.android.referral.ui.referral_target_user_list.views.UnconvertedTargetedUserItemView
import tech.okcredit.android.referral.ui.share.ShareReferralFragment
import javax.inject.Inject

class ReferralTargetedUsersListFragment :
    BaseFragment<State, ViewEvents, Intent>(
        "ReferralTargetedUsersListScreen",
        R.layout.fragment_targeted_user_list,
    ),
    UnconvertedTargetedUserItemView.UnconvertedTargetedUserActionListener,
    ConvertedTargetedUserItemView.ConvertedTargetedUserActionListener {

    private val binding: FragmentTargetedUserListBinding by viewLifecycleScoped(FragmentTargetedUserListBinding::bind)

    @Inject
    lateinit var targetedListController: Lazy<TargetedListController>

    @Inject
    lateinit var referralEventTracker: Lazy<ReferralEventTracker>

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.shareAsStatus.clicks().map {
                referralEventTracker.get()
                    .trackReferralScreenInteracted("Generic Share", ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION)
                referralEventTracker.get()
                    .trackShareReferral(PropertyValue.REFERRAL, ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION)
                Intent.ShareReferral
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.inviteTargetedUserList.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = targetedListController.get().adapter
        }
    }

    override fun render(state: State) {
        binding.progressBar.isVisible = state.showProgress

        renderGenericShare(state.showGenericShare)

        state.showTargetedUserList?.also { show ->
            if (show) {
                binding.apply {
                    shareFragment.gone()
                    knowMoreFragment.visible()
                    targetedUserViews.visible()

                    state.targetedUsers?.apply {
                        if (isNotEmpty() && all { it.converted }) {
                            inviteFormListTitle.setText(R.string.targeted_referral_all_invited_title)
                            inviteFormListSubtitle.visible()
                        } else {
                            inviteFormListTitle.setText(R.string.invite_from_the_list)
                            inviteFormListSubtitle.gone()
                        }
                    }
                }
                setControllerTargetedUserList(state.targetedUsers)
            } else {
                binding.apply {
                    // Share fragment has it's own know more section
                    shareFragment.visible()
                    knowMoreFragment.gone()
                    targetedUserViews.gone()
                }
                replaceFragment(ShareReferralFragment(), R.id.share_fragment)
            }
        }
    }

    private fun renderGenericShare(canShow: Boolean) {
        binding.referralLinkContainer.isVisible = canShow
    }

    private fun setControllerTargetedUserList(targetedUsers: List<TargetedUser>?) {
        targetedListController.get().setTargetedUserList(targetedUsers)
    }

    override fun onInviteButtonClicked(targetUser: TargetedUser) {
        val version = if (getCurrentState().showGenericShare) {
            ReferralVersion.TARGETED_REFERRAL_WITH_SHARE_OPTION
        } else {
            ReferralVersion.TARGETED_REFERRAL
        }
        referralEventTracker.get().trackShareReferral(PropertyValue.REFERRAL, version)
        referralEventTracker.get().trackReferralScreenInteracted("Invite User", version)
        pushIntent(Intent.SendInviteToWhatsApp(targetUser))
    }

    override fun onRequestToOpen(targetUser: TargetedUser) {
        pushIntent(Intent.GoToReferralRewardsForPhoneNumber(targetUser.phoneNumber))
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {
            is ViewEvents.ReferralIntent -> startActivity(event.shareIntent)
            is ViewEvents.ShareFailure -> shortToast(R.string.share_referral_failure)
            is ViewEvents.GoToReferralRewardsForPhoneNumber -> navigate(
                R.id.referredMerchantListScreen,
                bundleOf(ARG_TARGETED_REFERRAL_PHONE_NUMBER to event.phoneNumber)
            )
        }
    }
}
