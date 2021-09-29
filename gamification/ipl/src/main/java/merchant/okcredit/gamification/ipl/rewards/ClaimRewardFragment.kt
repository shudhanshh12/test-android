package merchant.okcredit.gamification.ipl.rewards

import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.rewards.contract.RewardType
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.res.ColorStateList
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.anupkumarpanwar.scratchview.ScratchView
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import merchant.okcredit.gamification.ipl.BuildConfig
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.ClaimRewardFragmentBinding
import merchant.okcredit.gamification.ipl.game.ui.GameFragment
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract.*
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardContract.RewardClaimErrorState.*
import merchant.okcredit.gamification.ipl.utils.IplUtils
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import javax.inject.Inject

class ClaimRewardFragment : BaseFragment<State, ViewEvent, Intent>(
    "ClaimRewardScreen",
    R.layout.claim_reward_fragment
) {

    companion object {
        fun newInstance() = ClaimRewardFragment()
    }

    private val scratchViewRevealListener = object : ScratchView.IRevealListener {
        override fun onRevealed(scratchView: ScratchView?) {
            pushIntent(Intent.Reveal)
            binding.hint.gone()
        }

        override fun onRevealPercentChangedListener(scratchView: ScratchView?, percent: Float) {
            if (percent > 0.35) {
                scratchView?.reveal()
            }
        }
    }

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private var whatsappMesage = ""
    private var rewardType = ""

    internal val binding: ClaimRewardFragmentBinding by viewLifecycleScoped(ClaimRewardFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initListener()
    }

    override fun onStop() {
        (binding as ClaimRewardFragmentBinding?)?.scratchView?.setRevealListener(null)
        super.onStop()
    }

    private fun init() {
        setStatusBarColor(R.color.grey800)
        binding.apply {
            tvAddAddress.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            tvContactUsWhastapp.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun initListener() {
        binding.layout.setOnClickListener { requireActivity().finish() }

        binding.tvAddAddress.setOnClickListener { goToMerchantAddressScreen() }

        binding.tvContactUsWhastapp.setOnClickListener {
            openWhatsApp(whatsappMesage)
        }

        binding.scratchView.setRevealListener(scratchViewRevealListener)

        binding.tvEditBankDetail.setOnClickListener {
            showAddPaymentDetailsDialog(true)
        }
    }

    private fun showAddPaymentDetailsDialog(updated: Boolean = false) {
        collectionNavigator.get()
            .showMerchantDestinationDialog(
                childFragmentManager,
                source = IplEventTracker.Value.SOURCE_IPL_REWARDS,
                isUpdateCollection = updated
            )
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {

        if (state.claimed || !state.canShowScratchView) {
            binding.scratchView.reveal()

            if (state.nonCashReward) {
                showNonCashRewardsOptions()
            }
        }

        if (state.claimInProgress) {
            binding.progressViews.visible()
        } else {
            binding.progressViews.gone()
        }

        when (state.rewardType) {
            RewardType.IPL_BAT -> {
                rewardType = getString(R.string.mini_bat)
                binding.batReward.reward.visible()
            }
            RewardType.IPL_TSHIRT -> {
                rewardType = getString(R.string.t_shirt)
                binding.tshirtReward.reward.visible()
            }
            RewardType.IPL_DAILY, RewardType.IPL_WEEKLY -> {
                if (state.zeroReward) {
                    binding.zeroReward.reward.visible()
                } else {
                    binding.cashReward.apply {
                        reward.visible()
                        amount.text = getString(R.string.rupee_placeholder, state.displayAmount)
                    }
                }
            }
            else -> {
                // do nothing
            }
        }
        handleClaimRewardErrorState(state.rewardClaimErrorState, state.customerMessage)
    }

    private fun handleClaimRewardErrorState(rewardClaimErrorState: RewardClaimErrorState?, customMessage: String?) {
        binding.apply {
            when (rewardClaimErrorState) {
                PROCESSING_PAYOUT_DELAYED, FAILED_BANK_UNAVAILABLE -> {
                    errorSolveIssue.visible()
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
                }
                PROCESSING_PAYOUT_STARTED -> {
                    errorSolveIssue.gone()
                }
                PROCESSING_BUDGET_EXHAUSTED -> {
                    errorSolveIssue.visible()
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
                }
                PROCESSING_CUSTOM_MESSAGE -> {
                    errorSolveIssue.visible()
                    val colorStateList = ColorStateList.valueOf(getColorCompat(R.color.indigo_1))
                    TextViewCompat.setCompoundDrawableTintList(
                        tvSolveIssue,
                        colorStateList
                    )
                    tvSolveIssue.setTextColor(colorStateList)
                    errorDescription.setTextColor(getColorCompat(R.color.indigo_lite))
                    tvSolveIssue.text = getString(R.string.reward_update)
                    errorDescription.text = customMessage
                    tvEditBankDetail.invisible()
                }
                ON_HOLD_UPI_INACTIVE -> {
                    errorSolveIssue.visible()
                    errorDescription.text = getString(R.string.upi_inactive_error_msg)
                }
                ON_HOLD_DAILY_LIMIT_REACHED -> {
                    errorSolveIssue.visible()
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
                }
                ON_HOLD_CUSTOM_MESSAGE, FAILED_BANK_CUSTOM_MESSAGE -> {
                    errorSolveIssue.visible()
                    tvSolveIssue.text = getString(R.string.please_try_again_later)
                    errorDescription.text = customMessage
                    tvEditBankDetail.invisible()
                }
                ON_HOLD_BANK_DETAILS_DUPLICATE -> {
                    errorSolveIssue.visible()
                }
                else -> errorSolveIssue.gone()
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.Congratulate -> shortToast(R.string.congratulations)
            is ViewEvent.NonCashRewardWon -> {
                showNonCashRewardsOptions()
            }
            is ViewEvent.ShowAddPaymentDetailsDialog -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    showAddPaymentDetailsDialog(false)
                }
            }
            is ViewEvent.InternetIssue -> {
                shortToast(R.string.no_internet_msg)
                requireActivity().finish()
            }
            is ViewEvent.ServerError -> {
                shortToast(R.string.reward_claim_failure)
                requireActivity().finish()
            }
            is ViewEvent.DebugClaimStatus -> if (BuildConfig.DEBUG) shortToast(event.status)
        }
    }

    private fun showNonCashRewardsOptions() {
        showNonCashRewardAddressOptions()
        renderAddressText()
    }

    private fun goToMerchantAddressScreen() {
        Permission.requestLocationPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {}

                override fun onPermissionGranted() {
                    goMerchantInputScreen()
                }

                override fun onPermissionDenied() {
                    goMerchantInputScreen()
                }

                override fun onPermissionPermanentlyDenied() {}
            }
        )
    }

    internal fun goMerchantInputScreen() {
        legacyNavigator.get().goToMerchantInputScreenForResult(
            fragment = this,
            inputType = BusinessConstants.ADDRESS,
            inputTitle = getString(R.string.address),
            requestCode = GameFragment.REQUEST_MERCHANT_INPUT_SCREEN
        )
        requireActivity().finish()
    }

    private fun openWhatsApp(whatsappMesage: String) {
        val uri = Uri.parse("whatsapp://send")
            .buildUpon()
            .appendQueryParameter("text", whatsappMesage)
            .appendQueryParameter("phone", "91${IplUtils.IPL_WHATS_APP_HELP_NUMBER}")
            .build()

        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        intent.data = uri
        val packageManager = activity?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            shortToast(R.string.whatsapp_not_installed)
        }
        requireActivity().finish()
    }

    private fun showNonCashRewardAddressOptions() {
        binding.apply {
            tvAddAddressHint.visible()
            tvAddAddress.visible()
            tvContactUsWhastapp.visible()
        }
    }

    private fun renderAddressText() {
        binding.apply {
            if (getCurrentState().merchantAddress.isNullOrBlank()) {
                tvAddress.gone()
                tvContactUsWhastapp.text = getString(R.string.whatsapp_your_address)
                tvAddAddressHint.text = getString(R.string.add_address_hint, rewardType)
                tvAddAddress.text = getString(R.string.add_address)
                whatsappMesage = getString(R.string.whatsapp_message_send_address)
            } else {
                tvAddress.visible()
                tvContactUsWhastapp.text = getString(R.string.contact_us_whatsapp)
                tvAddAddressHint.text = getString(R.string.correct_address_hint)
                tvAddAddress.text = getString(R.string.confirm_address)
                tvAddress.text = getCurrentState().merchantAddress
                whatsappMesage =
                    getString(R.string.whatsapp_message_deliver_price, rewardType, getCurrentState().merchantAddress)
            }
        }
    }

    fun pushIntentForClaimReward() {
        pushIntent(Intent.ClaimReward)
    }
}
