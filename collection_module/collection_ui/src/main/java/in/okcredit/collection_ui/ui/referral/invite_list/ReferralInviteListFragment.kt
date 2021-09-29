package `in`.okcredit.collection_ui.ui.referral.invite_list

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionEventTracker
import `in`.okcredit.collection.contract.TargetedCustomerReferralInfo
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.databinding.ReferralInviteListFragmentBinding
import `in`.okcredit.collection_ui.ui.referral.invite_list.views.TargetedReferralInviteView
import `in`.okcredit.rewards.contract.RewardsNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReferralInviteListFragment :
    BaseFragment<ReferralInviteListContract.State, ReferralInviteListContract.ViewEvents, ReferralInviteListContract.Intent>(
        "SupplierEditAmountBottomSheet",
        R.layout.referral_invite_list_fragment
    ),
    TargetedReferralInviteView.Listener {

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var rewardsNavigator: Lazy<RewardsNavigator>

    private val controller = ReferralInviteListController(this)

    private val binding: ReferralInviteListFragmentBinding by viewLifecycleScoped(ReferralInviteListFragmentBinding::bind)

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(ReferralInviteListContract.Intent.Resume),
            binding.mbTotalReward.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ReferralInviteListContract.Intent.GotoRewardScreen
                },
            binding.ivHelp.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ReferralInviteListContract.Intent.HelpClicked
                },
        )
    }

    override fun loadIntent(): UserIntent {
        return ReferralInviteListContract.Intent.Load
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    override fun render(state: ReferralInviteListContract.State) {
        setEarnedRewardButtonUi(state.rewardAmount)
        controller.setData(state.list)
        setGotoRewardButtonUi(state)
    }

    private fun setGotoRewardButtonUi(state: ReferralInviteListContract.State) {
        binding.mbTotalReward.apply {
            if (state.rewardBtnActive) {
                isEnabled = true
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_primary))
            } else {
                isEnabled = false
                backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey500))
            }
        }
    }

    private fun initList() {
        binding.rvInviteTargetedUserList.adapter = controller.adapter
    }

    private fun setEarnedRewardButtonUi(amount: Long) {
        binding.apply {
            tvEarnAmount.text = getString(
                R.string.earned_amount,
                CurrencyUtil.formatV2(amount)
            )
        }
    }

    override fun handleViewEvent(event: ReferralInviteListContract.ViewEvents) {
        when (event) {
            is ReferralInviteListContract.ViewEvents.InviteOnWhatsApp -> {
                startActivity(event.intent)
            }
            ReferralInviteListContract.ViewEvents.GotoRewardScreen -> rewardsNavigator.get()
                .goToRewardsScreen(requireContext())
            is ReferralInviteListContract.ViewEvents.HelpClicked -> helpClicked(event.ids)
            ReferralInviteListContract.ViewEvents.ShowWhatsAppError -> shortToast(requireContext().getString(R.string.whatsapp_not_installed))
            ReferralInviteListContract.ViewEvents.ShowSomethingWrongError -> shortToast(requireContext().getString(R.string.err_default))
        }
    }

    override fun inviteOnWhatsApp(targetedCustomerReferralInfo: TargetedCustomerReferralInfo) {
        pushIntent(ReferralInviteListContract.Intent.OnInviteBtnClicked(targetedCustomerReferralInfo))
    }

    private fun helpClicked(contextualHelpIds: List<String>) {
        if (contextualHelpIds.isNotEmpty()) {
            legacyNavigator.get().goToHelpV2Screen(
                requireContext(), contextualHelpIds,
                source = if (getCurrentState().customerIdFrmLedger.isNotNullOrBlank()) CollectionEventTracker.CUSTOMER_SCREEN else CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN,
            )
        }
    }
}
