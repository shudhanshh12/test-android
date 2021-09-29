package `in`.okcredit.merchant.rewards.ui.rewards_dialog

import `in`.okcredit.analytics.Event
import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.merchant.rewards.BuildConfig
import `in`.okcredit.merchant.rewards.R
import `in`.okcredit.merchant.rewards.analytics.RewardsEventTracker
import `in`.okcredit.merchant.rewards.databinding.RewardsDialogBinding
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsContract.State
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardsContract.ViewEvents
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.rewards.contract.RewardsNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.anupkumarpanwar.scratchview.ScratchView
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.DateTimeUtils
import java.lang.ref.WeakReference
import javax.inject.Inject

class ClaimRewardsDialog : BaseFragment<State, ViewEvents, ClaimRewardsContract.Intent>(
    "Rewards Dialog",
    R.layout.rewards_dialog
) {

    @Inject
    lateinit var deviceUtils: Lazy<DeviceUtils>

    @Inject
    lateinit var rewardsNavigator: Lazy<RewardsNavigator>

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var tracker: Lazy<RewardsEventTracker>

    private val binding: RewardsDialogBinding by viewLifecycleScoped(RewardsDialogBinding::bind)

    private val scratchViewRevealListener = object : ScratchView.IRevealListener {
        override fun onRevealed(scratchView: ScratchView?) {
            pushIntentForRevealReward()
        }

        override fun onRevealPercentChangedListener(scratchView: ScratchView?, percent: Float) {

            if (!getCurrentState().isScratchViewPartiallyRevealed) {
                pushIntentForScratchViewPartiallyRevealed()
            }

            if (percent > 0.35) {
                scratchView?.reveal()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupScratchViewListener()
        binding.rootView.setTracker(performanceTracker)
    }

    private fun setupScratchViewListener() {
        binding.scratchView.setRevealListener(scratchViewRevealListener)
    }

    private fun setupClickListeners() {
        binding.ctaBtn.setOnClickListener {
            when {
                getCurrentState().isFeatureReward -> {
                    tracker.get().trackRewardDialogInteracted(
                        item = "View Feature Button"
                    )
                    pushIntent(
                        ClaimRewardsContract.Intent.NudgeFeature(
                            getCurrentState().featureDetails?.deepLink
                                ?: ""
                        )
                    )
                }
                getCurrentState().isBetterLuckNextTimeReward -> {
                    tracker.get().trackRewardDialogInteracted(
                        item = "Close Button"
                    )
                    requireActivity().finish()
                }
                else -> {
                    tracker.get().trackRewardDialogInteracted(
                        item = "Share Button",
                        amount = getCurrentState().displayAmount,
                        type = getCurrentState().rewardType.type
                    )
                    pushIntent(ClaimRewardsContract.Intent.ShareReward)
                }
            }
        }
        binding.ivCancel.setOnClickListener {
            if (getCurrentState().isScratchViewPartiallyRevealed && getCurrentState().showScratchView) {
                pushIntentForRevealReward()
            } else {
                pushIntentForFinishActivity()
            }
        }
        binding.tvEditBankDetail.setOnClickListener {
            tracker.get().trackRewardDialogInteracted(
                item = "Edit Bank Detail Button",
                amount = getCurrentState().displayAmount,
                type = getCurrentState().rewardType.type
            )
            pushIntent(ClaimRewardsContract.Intent.SetBankDetails)
        }
        binding.enterBankDetailsBtn.setOnClickListener {
            pushIntent(ClaimRewardsContract.Intent.SetBankDetails)
        }
        binding.goToRewardsButton.setOnClickListener {
            pushIntent(ClaimRewardsContract.Intent.GoToRewardsScreen)
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent {
        return ClaimRewardsContract.Intent.Load
    }

    override fun render(state: State) {
        binding.apply {

            rewardErrorHandling(state)

            if (state.showEnterBankDetailsButton) {
                enterBankDetailsGroup.visible()
                hurreyTv.visible()
            } else {
                enterBankDetailsGroup.gone()
            }

            if (state.rewardType == RewardType.BETTER_LUCK_NEXT_TIME) {
                paidOn.text = getString(R.string.scratched_on, DateTimeUtils.format(state.createTime))
            } else {
                paidOn.text = getString(R.string.paid_on, DateTimeUtils.format(state.createTime))
            }

            if (!state.isFeatureReward ||
                !state.isBetterLuckNextTimeReward
            ) {
                winAmount.text = getString(R.string.you_won, state.displayAmount)
            }

            when (state.rewardType) {
                RewardType.Collection_ADOPTION_REWARDS -> {
                    tvDescription.text = getString(R.string.earned_for_opting, getString(R.string.online_collections))
                }
                RewardType.PAY_ONLINE_CASHBACK_REWARDS -> {
                    tvDescription.text = getString(R.string.earned_for_making_online_payment)
                }
                RewardType.REFERRAL_REWARDS -> {
                    tvDescription.text = getString(R.string.earned_for_opting, getString(R.string.refferal))
                }
                RewardType.DIGITAL_SERVICE_SURVEY_REWARD -> {
                    tvDescription.text = getString(R.string.earned_for_opting_survey_response)
                }
                RewardType.BETTER_LUCK_NEXT_TIME -> {
                    winAmount.visible()
                    winAmount.text = getString(R.string.better_luck_next_time)
                }
                RewardType.ACTIVATION_MONEY_REWARDS,
                RewardType.ACTIVATION_FEATURE_REWARDS,
                -> {
                    tvDescription.text = getString(R.string.earned_due_to_activation)
                }
                RewardType.COLLECTION_TARGETED_REFERRAL -> {
                    tvDescription.text = getString(R.string.enable_for_referring_merchant)
                }
                else -> {
                    tvDescription.gone()
                }
            }

            if (state.isFeatureReward &&
                state.featureDetails != null
            ) {
                when {
                    state.featureDetails.featureDescription.isNotEmpty() -> {
                        tvDescription.text = state.featureDetails.featureDescription
                    }
                    state.featureDetails.featureIcon.isNotEmpty() -> {
                        GlideApp
                            .with(requireContext())
                            .load(state.featureDetails.featureIcon)
                            .placeholder(R.drawable.ic_reward_giftbox)
                            .error(R.drawable.ic_reward_giftbox)
                            .fallback(R.drawable.ic_reward_giftbox)
                            .centerCrop()
                            .into(imageView)
                    }
                }
                winAmount.text = getString(R.string.you_won_feature, state.featureDetails.featureTitle)
                ctaBtn.text = getString(R.string.visit_feature, state.featureDetails.featureTitle)
                captionCreditIn48Hours.invisible()
            }

            if (state.showScratchView) {
                rootView.children.iterator().forEach { it.invisible() }
                viewGroupForUnscratchedReward.visible()
            } else {
                scratchViewGroup.gone()
            }
            // Note: ^^^ This if/else requires to be the last in the ladder, please add you code prior to this.
            // TODO: Refactor and improve this and overall reward state management
        }
    }

    private fun rewardErrorHandling(state: State) = binding.apply {
        when {
            state.isBetterLuckNextTimeReward -> {
                progress.gone()
                winAmount.visible()
                tvDescription.gone()
                paidOn.gone()
                imageView.setImageDrawable(getDrawableCompact(R.drawable.ic_better_luck_next_time))
                hurreyTv.text = getString(R.string.oops)
                hurreyTv.visible()
                ctaBtn.text = getString(R.string.close)
                ctaBtn.visible()
            }
            state.claimApiProcessingState -> {
                progress.visible()
                hurreyTv.visible()
            }
            state.networkError -> {
                longToast(R.string.home_no_internet_msg)
                requireActivity().finish()
            }
            state.claimed -> {
                progress.gone()
                hurreyTv.visible()
                winAmount.visible()
                tvDescription.visible()
                paidOn.visible()
                ctaBtn.visible()
                goToRewardsButton.isVisible = state.canShowGoToRewardsButton
                errorSolveIssue.gone()
            }
            state.isBankDetailsDuplication -> {
                errorSolveIssue.visible()
                progress.gone()
                winAmount.visible()
                tvDescription.visible()
                hurreyTv.visible()
                paidOn.visible()
                ctaBtn.gone()
            }
            state.isUpiInactive -> {
                errorSolveIssue.visible()
                progress.gone()
                winAmount.visible()
                tvDescription.visible()
                hurreyTv.visible()
                paidOn.visible()
                errorDescription.text = getString(R.string.upi_inactive_error_msg)
                ctaBtn.gone()
            }
            state.isDailyPayoutLimitReached -> {
                errorSolveIssue.visible()
                progress.gone()
                winAmount.visible()
                tvDescription.visible()
                hurreyTv.visible()
                paidOn.visible()
                val colorStateList = ColorStateList.valueOf(getColorCompat(R.color.orange_primary))
                TextViewCompat.setCompoundDrawableTintList(
                    tvSolveIssue,
                    colorStateList
                )
                errorDescription.setTextColor(getColorCompat(R.color.orange_lite_1))
                tvSolveIssue.setTextColor(colorStateList)
                tvSolveIssue.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_info, 0, 0, 0)
                tvSolveIssue.text = getString(R.string.please_try_again_later)
                errorDescription.text = getString(R.string.payout_daily_limit_reached_error_msg)
                tvEditBankDetail.invisible()
                ctaBtn.gone()
            }
            state.isBudgetExhausted -> {
                errorSolveIssue.visible()
                progress.gone()
                tvDescription.visible()
                winAmount.visible()
                hurreyTv.visible()
                paidOn.visible()
                val colorStateList = ColorStateList.valueOf(getColorCompat(R.color.indigo_1))
                TextViewCompat.setCompoundDrawableTintList(
                    tvSolveIssue,
                    colorStateList
                )
                tvSolveIssue.setTextColor(colorStateList)
                errorDescription.setTextColor(getColorCompat(R.color.indigo_lite))
                tvSolveIssue.text = getString(R.string.reward_update)
                errorDescription.text = getString(R.string.we_are_processing_your_reward)
                tvEditBankDetail.invisible()
                ctaBtn.gone()
            }
            state.isPayoutInitiated -> {
                errorSolveIssue.gone()
                progress.gone()
                tvDescription.visible()
                winAmount.visible()
                hurreyTv.visible()
                paidOn.visible()
                captionCreditIn48Hours.visible()
                ctaBtn.visible()
                goToRewardsButton.isVisible = state.canShowGoToRewardsButton
            }
            state.isPayoutDelayed
                || state.isFailedBankUnavailable -> {
                errorSolveIssue.visible()
                progress.gone()
                tvDescription.visible()
                winAmount.visible()
                hurreyTv.visible()
                paidOn.visible()
                val colorStateList = ColorStateList.valueOf(getColorCompat(R.color.orange_primary))
                TextViewCompat.setCompoundDrawableTintList(
                    tvSolveIssue,
                    colorStateList
                )
                tvSolveIssue.setTextColor(colorStateList)
                errorDescription.setTextColor(getColorCompat(R.color.orange_lite))
                tvSolveIssue.text = getString(R.string.bank_service_unavail)
                errorDescription.text = getString(R.string.bank_service_unavil_error_msg)
                tvEditBankDetail.invisible()
                ctaBtn.gone()
            }
            state.isOnHoldCustomMessageAvailable -> {
                winAmount.visible()
                tvDescription.visible()
                hurreyTv.visible()
                paidOn.visible()
                val colorStateList = ColorStateList.valueOf(getColorCompat(R.color.orange_primary))
                TextViewCompat.setCompoundDrawableTintList(
                    tvSolveIssue,
                    colorStateList
                )
                errorDescription.setTextColor(getColorCompat(R.color.orange_lite_1))
                tvSolveIssue.setTextColor(colorStateList)
                tvSolveIssue.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_info, 0, 0, 0)
                tvSolveIssue.text = getString(R.string.please_try_again_later)
                errorDescription.text = state.customMessages
                tvEditBankDetail.invisible()
                ctaBtn.gone()
            }
            state.isProcessingCustomMessageAvailable -> {
                errorSolveIssue.visible()
                progress.gone()
                tvDescription.visible()
                winAmount.visible()
                hurreyTv.visible()
                paidOn.visible()
                val colorStateList = ColorStateList.valueOf(getColorCompat(R.color.indigo_1))
                TextViewCompat.setCompoundDrawableTintList(
                    tvSolveIssue,
                    colorStateList
                )
                tvSolveIssue.setTextColor(colorStateList)
                errorDescription.setTextColor(getColorCompat(R.color.indigo_lite))
                tvSolveIssue.text = getString(R.string.reward_update)
                errorDescription.text = state.customMessages
                tvEditBankDetail.invisible()
                ctaBtn.gone()
            }
            state.isFailedCustomMessageAvailable -> {
                errorSolveIssue.visible()
                progress.gone()
                tvDescription.visible()
                winAmount.visible()
                hurreyTv.visible()
                paidOn.visible()
                tvSolveIssue.text = getString(R.string.please_try_again_later)
                errorDescription.text = state.customMessages
                tvEditBankDetail.invisible()
                ctaBtn.gone()
            }
        }
    }

    override fun handleViewEvent(event: ViewEvents) {
        when (event) {

            is ViewEvents.ShareReward -> shareReward(event.intent)
            is ViewEvents.GoToRewardsScreen -> goToRewardsScreen()
            is ViewEvents.ShowAddMerchantDestinationDialog -> showAddMerchantDestinationDialog()
            is ViewEvents.ClaimFailure -> claimFailure()
            is ViewEvents.DebugClaimStatus -> if (BuildConfig.DEBUG) shortToast(event.status)
            is ViewEvents.FinishActivity -> requireActivity().finish()
        }
    }

    override fun onStop() {
        val _binding = WeakReference(binding)
        _binding.get()?.scratchView?.setRevealListener(null)
        super.onStop()
    }

    private fun claimFailure() {
        binding.progress.gone()
        pushIntent(ClaimRewardsContract.Intent.SetError)
        requireActivity().finish()
    }

    private fun shareReward(intent: Intent) {
        startActivity(intent)
    }

    private fun goToRewardsScreen() {
        rewardsNavigator.get().goToRewardsScreen(requireContext())
    }

    private fun showAddMerchantDestinationDialog() {
        collectionNavigator.get().showMerchantDestinationDialog(
            fragmentManager = childFragmentManager,
            asyncRequest = true,
            source = Event.REWARD_SCREEN
        )
    }

    fun pushIntentForClaimReward() {
        pushIntent(ClaimRewardsContract.Intent.ClaimReward)
    }

    fun pushIntentForRevealReward() {
        pushIntent(ClaimRewardsContract.Intent.RevealReward)
    }

    private fun pushIntentForFinishActivity() {
        pushIntent(ClaimRewardsContract.Intent.FinishActivity)
    }

    fun pushIntentForScratchViewPartiallyRevealed() {
        pushIntent(ClaimRewardsContract.Intent.ScratchViewPartiallyRevealed)
    }
}
