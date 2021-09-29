package `in`.okcredit.payment.ui.payment_result

import `in`.okcredit.analytics.Event
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.payment.R
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.DONE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.RETRY
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.SHARE
import `in`.okcredit.payment.contract.PaymentResultListener
import `in`.okcredit.payment.contract.model.JuspayPollingStatus
import `in`.okcredit.payment.databinding.PaymentResultFragmentBinding
import `in`.okcredit.payment.utils.CurrencyUtil
import `in`.okcredit.payment.utils.getDateTimeString
import `in`.okcredit.payment.utils.getWhatsAppMsg
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardsNavigator
import `in`.okcredit.rewards.contract.getAmountInRupees
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import merchant.okcredit.accounting.contract.model.SupportType
import org.joda.time.DateTime
import tech.okcredit.android.base.TempCurrencyUtil.formatV2
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PaymentResultFragment :
    BaseFragment<PaymentResultContract.State, PaymentResultContract.ViewEvents, PaymentResultContract.Intent>(
        "PaymentResultFragment"
    ) {

    companion object {
        const val ARG_PAYMENT_ID_PAYMENT_RESULT = "payment_id"
        const val ARG_PAYMENT_TYPE_RESULT = "payment_type"
        const val ARG_PAYMENT_SHOW_TXN_CANCELLED = "txn_cancelled"

        fun getAnalyticsPropertyValueForScreenType(uiScreenType: PaymentResultContract.UiScreenType): String {
            return when (uiScreenType) {
                PaymentResultContract.UiScreenType.SUCCESS -> PaymentAnalyticsEvents.PaymentPropertyValue.SUCCESSFUL
                PaymentResultContract.UiScreenType.FAILED -> PaymentAnalyticsEvents.PaymentPropertyValue.FAILED
                PaymentResultContract.UiScreenType.PENDING -> PaymentAnalyticsEvents.PaymentPropertyValue.PENDING
                PaymentResultContract.UiScreenType.CANCELLED -> PaymentAnalyticsEvents.PaymentPropertyValue.CANCELLED
                PaymentResultContract.UiScreenType.LOADING -> ""
            }
        }
    }

    private var paymentListener: PaymentResultListener? = null

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var rewardsNavigator: Lazy<RewardsNavigator>

    @Inject
    lateinit var paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>

    internal var animatedVectorDrawableCompat: AnimatedVectorDrawableCompat? = null
    private var animationCallback: Animatable2Compat.AnimationCallback? = null

    private var hasShownRewardPopUpOnce = false

    internal val binding: PaymentResultFragmentBinding by viewLifecycleScoped(
        PaymentResultFragmentBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return PaymentResultFragmentBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PaymentResultListener) {
            setPaymentResultListener(context)
        }
    }

    override fun onDetach() {
        paymentListener = null
        super.onDetach()
    }

    private fun setPaymentResultListener(listener: PaymentResultListener) {
        paymentListener = listener
    }

    private fun initLoader() {
        animatedVectorDrawableCompat =
            AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.payment_loading_anim)
        registerCallback()
    }

    private fun registerCallback() {
        animatedVectorDrawableCompat?.let {
            binding.ivLoader.setImageDrawable(it)
            it.start()
            animationCallback = object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    animatedVectorDrawableCompat?.start()
                }
            }
            it.registerAnimationCallback(animationCallback!!)
        }
    }

    private fun unRegisterCallback() {
        animationCallback?.let {
            animatedVectorDrawableCompat?.unregisterAnimationCallback(it)
        }
    }

    override fun onPause() {
        super.onPause()
        unRegisterCallback()
    }

    override fun onResume() {
        super.onResume()
        pushIntent(PaymentResultContract.Intent.TrackPaymentRewardStatusPageView)
        if (binding.ivLoader.isVisible()) {
            initLoader()
        }
    }

    private fun setClickListeners() {
        binding.apply {
            ivCross.setOnClickListener {
                requireActivity().finish()
            }

            mbDone.setOnClickListener {
                getCurrentState().let {
                    paymentAnalyticsEvents.get().trackPaymentStatusClick(
                        accountId = it.accountId,
                        screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                        relation = it.getRelationFrmAccountType(),
                        flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                        action = DONE,
                        easyPay = it.blindPayFlow,
                        status = getAnalyticsPropertyValueForScreenType(it.uiScreenType),
                    )
                }
                requireActivity().finish()
            }

            rewardDetails.setOnClickListener {
                getCurrentState().reward?.let {
                    pushIntent(PaymentResultContract.Intent.TrackPaymentRewardClicked)
                    goToClaimRewardScreen(it, getCurrentState().juspayPaymentPollingModel?.paymentId ?: "")
                }
            }

            buttonSupport.setOnClickListener {
                pushIntent(
                    PaymentResultContract.Intent.SupportClicked(
                        getCustomerSupportString(),
                        getCurrentState().supportNumber
                    )
                )
            }
        }
    }

    private fun copyTxnIdTOClipBoard(txnId: String) {
        val clpManager = requireContext().copyToClipboard(txnId)
        if (clpManager != null)
            shortToast(R.string.payment_copied_toast)
        else shortToast(R.string.payment_copy_error)
    }

    private fun takeScreenShot(shareLink: String?) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val screenShot = requireActivity().window.decorView.rootView
            val bitmap =
                Bitmap.createBitmap(screenShot.width, screenShot.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            screenShot.draw(canvas)
            pushIntent(
                PaymentResultContract.Intent.ShareScreenShot(
                    bitmap,
                    blindPayShareText(shareLink)
                )
            )
        }
    }

    private fun blindPayShareText(shareLink: String?): String? {
        if (!getCurrentState().blindPayFlow || shareLink.isNullOrEmpty()) return null

        return getCurrentState().juspayPaymentPollingModel?.let {
            getString(
                R.string.payment_blind_pay_share_link_msg, getCurrentState().name,
                formatV2(it.paymentInfo.paymentAmount?.toLong() ?: 0L),
                shareLink
            )
        }
    }

    private fun openWhatsAppToShareScreenshot(intent: Intent) {
        startActivity(intent)
    }

    private fun goToClaimRewardScreen(reward: RewardModel, paymentId: String) {
        pushIntent(PaymentResultContract.Intent.SetRewardPopUpShownAtLeastOnce)
        rewardsNavigator.get().goToClaimRewardScreen(
            requireActivity(), reward, Event.PAYMENT_RESULT_SCREEN, paymentId
        )
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.tvCopy.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PaymentResultContract.Intent.CopyTxnIdToClipBoard
                },
            binding.mbShare.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    if (getCurrentState().juspayPaymentPollingModel?.status == JuspayPollingStatus.FAILED.value || getCurrentState().uiScreenType == PaymentResultContract.UiScreenType.CANCELLED) {
                        getCurrentState().let {
                            paymentAnalyticsEvents.get().trackPaymentStatusClick(
                                accountId = it.accountId,
                                screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                                relation = it.getRelationFrmAccountType(),
                                flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                                action = RETRY,
                                easyPay = it.blindPayFlow,
                                status = getAnalyticsPropertyValueForScreenType(it.uiScreenType),
                            )
                        }
                    } else {
                        getCurrentState().let {
                            paymentAnalyticsEvents.get().trackPaymentStatusClick(
                                accountId = it.accountId,
                                screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_STATUS,
                                relation = it.getRelationFrmAccountType(),
                                flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                                action = SHARE,
                                easyPay = it.blindPayFlow,
                                status = getAnalyticsPropertyValueForScreenType(it.uiScreenType),
                            )
                        }
                    }
                    PaymentResultContract.Intent.ClickedShareOrRetry
                },
        )
    }

    private fun getCustomerSupportString(): String {
        return getCurrentState().juspayPaymentPollingModel?.let {
            getWhatsAppMsg(
                requireContext(),
                amount = CurrencyUtil.formatV2(it.paymentInfo.paymentAmount?.toLong() ?: 0L),
                paymentTime = DateTimeUtils.formatLong(DateTime(it.paymentInfo.createTime.times(1000))),
                txnId = it.paymentId,
                status = it.status.capitalizeWords()
            )
        } ?: getString(R.string.t_002_i_need_help_generic)
    }

    override fun render(state: PaymentResultContract.State) {
        when (state.uiScreenType) {
            PaymentResultContract.UiScreenType.LOADING -> {
                handleLoadingTimer()
            }
            PaymentResultContract.UiScreenType.SUCCESS -> {

                var title = getString(R.string.payment_title_success)
                var shareIcon = R.drawable.payment_ic_share

                if (state.blindPayFlow) {
                    binding.tvPaidTo.text = getString(R.string.payment_blind_paid_to_success)
                    title = getString(R.string.payment_blind_pay_header_success)
                    shareIcon = R.drawable.ic_whatsapp_green
                }

                setUi(
                    state = state,
                    title = title,
                    icon = R.drawable.payment_ic_success,
                    shareIcon = shareIcon,
                    shareTitle = getString(R.string.payment_share)
                )
                handleRewardUi(state)
                setBlindPaySuccessUI(state)
            }
            PaymentResultContract.UiScreenType.PENDING -> {
                setCustomerSupportBannerUi(state.supportType)
                var title = getString(R.string.payment_title_pending)
                var msg = R.string.payment_result_pending
                var shareIcon = R.drawable.payment_ic_share

                if (state.blindPayFlow) {
                    binding.tvPaidTo.text =
                        getString(R.string.payment_blind_paid_to_pending, state.getRelationFrmAccountType())
                    title = getString(R.string.payment_blind_pay_header_pending)
                    msg = R.string.payment_blind_reason_pending
                    shareIcon = R.drawable.ic_whatsapp_green
                }

                setUi(
                    state = state,
                    title = title,
                    icon = R.drawable.payment_ic_pending,
                    msg = msg,
                    shareIcon = shareIcon,
                    shareTitle = getString(R.string.payment_share)
                )
            }
            PaymentResultContract.UiScreenType.FAILED -> {
                setCustomerSupportBannerUi(state.supportType)
                var title = getString(R.string.payment_title_failed)
                var msg = R.string.payment_result_failed

                if (state.blindPayFlow) {
                    binding.tvPaidTo.text =
                        getString(R.string.payment_blind_paid_to_pending, state.getRelationFrmAccountType())
                    title = getString(R.string.payment_blind_pay_header_failed)
                    msg = R.string.payment_blind_reason_failed
                }

                setUi(
                    state = state,
                    title = title,
                    icon = R.drawable.payment_ic_failed,
                    msg = msg,
                    shareIcon = R.drawable.payment_ic_loader,
                    shareTitle = getString(R.string.payment_retry)
                )
            }
            PaymentResultContract.UiScreenType.CANCELLED -> {
                binding.apply {
                    grpTxnCancelled.visible()
                    grpBottom.visible()
                    grpWait.gone()
                    mbShare.icon = getDrawableCompact(R.drawable.payment_ic_loader)
                    mbShare.text = getString(R.string.payment_retry)
                    tvTxnDate.text = System.currentTimeMillis().getDateTimeString()
                }
            }
        }
    }

    private fun setCustomerSupportBannerUi(supportType: SupportType) {
        when (supportType) {
            SupportType.CALL -> setSupportBannerUi(supportType)
            SupportType.CHAT -> setSupportBannerUi(supportType)
            SupportType.NONE -> {
                binding.apply {
                    buttonSupport.gone()
                }
            }
        }
    }

    private fun setSupportBannerUi(supportType: SupportType) {
        binding.apply {
            buttonSupport.visible()

            buttonSupport.text =
                if (supportType == SupportType.CALL) {
                    getString(
                        R.string.t_002_24X7help_banner_call_label,
                        getCurrentState().support24x7String,
                        getCurrentState().supportNumber
                    ).trim()
                } else
                    getString(
                        R.string.t_002_24X7help_banner_whatsapp_label,
                        getCurrentState().support24x7String,
                        getCurrentState().supportNumber
                    ).trim()

            buttonSupport.icon =
                getDrawableCompact(
                    if (supportType == SupportType.CALL)
                        R.drawable.ic_call_support_indigo
                    else R.drawable.ic_whatsapp_indigo
                )
        }
    }

    private fun setUi(
        state: PaymentResultContract.State,
        title: String,
        icon: Int,
        msg: Int? = null,
        shareIcon: Int,
        shareTitle: String,
    ) {

        binding.apply {
            state.juspayPaymentPollingModel?.let {
                grpResult.visible()
                grpBottom.visible()
                grpWait.gone()

                ivPaymentResult.setBackgroundResource(icon)
                tvTitleResult.text = title
                tvAmount.text = getString(
                    R.string.payment_edit_amount_rupee,
                    formatV2(it.paymentInfo.paymentAmount?.toLong() ?: 0L)
                )

                tvPaymentDate.text =
                    it.paymentInfo.createTime.times(1000).getDateTimeString()

                tvTxnId.text = getString(R.string.payment_txn_id, it.paymentId)

                msg?.let {
                    val tvMessageString = getString(it, "")
                    if (tvMessageString.isNotNullOrBlank()) {
                        tvMessage.text = tvMessageString
                    } else {
                        tvMessage.gone()
                    }
                }

                mbShare.icon = getDrawableCompact(shareIcon)
                mbShare.text = shareTitle
            }
        }

        state.let {
            binding.apply {
                tvAccountName.text = it.name
                val array = it.paymentAddress.split("@").toTypedArray()
                if (it.blindPayFlow) {
                    tvAccountId.text = "+91 ${it.mobile}"
                    return
                }

                when (it.destinationType) {
                    CollectionDestinationType.BANK.value -> {
                        if (array.isNotEmpty()) {
                            tvAccountId.text = array[0]
                            array.let {
                                if (it.size > 1) {
                                    tvAccountIfsc.text = it[1]
                                    tvAccountIfsc.visible()
                                }
                            }
                        }
                    }
                    else -> {
                        tvAccountId.text = it.paymentAddress
                        tvAccountIfsc.gone()
                    }
                }
            }
        }
    }

    private fun setBlindPaySuccessUI(state: PaymentResultContract.State) {
        binding.apply {
            if (!state.blindPayFlow) {
                blindPayEducationGroup.gone()
                return
            }

            blindPayEducationGroup.visible()
            if (state.isSupplier()) {
                tvDepositSubHeader.text = getString(
                    R.string.blind_pay_education_deposit_sub_header_supplier
                )
                tvRefundSubHeader.text = getString(R.string.blind_pay_education_refund_sub_header_supplier)
            } else {
                tvDepositSubHeader.text = getString(
                    R.string.blind_pay_education_deposit_sub_header_customer
                )
                tvRefundSubHeader.text = getString(R.string.blind_pay_education_refund_sub_header_customer)
            }
        }
    }

    private fun handleRewardUi(state: PaymentResultContract.State) {
        if (!hasShownRewardPopUpOnce) {
            hasShownRewardPopUpOnce = true
            pushIntent(
                PaymentResultContract.Intent.LoadRewardForPayment(
                    requireNotNull(state.juspayPaymentPollingModel?.paymentInfo?.id)
                )
            )
        }

        binding.apply {
            if (state.reward == null) {
                rewardGroup.gone()
                return
            }

            if (state.reward.isUnclaimed()) {
                rewardCaptionText.apply {
                    text = getString(R.string.tap_and_scratch_for_reward)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                    visible()
                }
                setRewardViewResources(
                    R.drawable.ic_gift_with_bg,
                    getString(R.string.you_won_cashback),
                )
            } else if (state.reward.isEditBankDetails()) {
                rewardCaptionText.apply {
                    text = getString(R.string.enter_bank_details_to_redeem_cashback)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
                    visible()
                }
                setRewardViewResources(
                    R.drawable.ic_cashback_won_with_bg,
                    getString(R.string.you_won_cashback_amount, state.reward.getAmountInRupees()),
                )
            } else {
                if (state.reward.isBetterLuckNextTimeReward()) {
                    rewardCaptionText.gone()
                    setRewardViewResources(
                        R.drawable.ic_better_luck_next_time_with_bg,
                        getString(R.string.better_luck_next_time),
                    )
                } else {
                    rewardCaptionText.apply {
                        text = getString(R.string.rewards_money_credited_72_hours)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                        visible()
                    }

                    if (!state.reward.isClaimed()) rewardCaptionText.gone()

                    setRewardViewResources(
                        R.drawable.ic_cashback_won_with_bg,
                        getString(R.string.you_won_cashback_amount, state.reward.getAmountInRupees()),
                    )
                }
            }
            rewardGroup.visible()
        }
    }

    private fun handleLoadingTimer() {
        getCurrentState().apply {
            when (loadingTimerState) {
                is PaymentResultContract.LoadingTimerState.TimerSet ->
                    binding.tvWait.text = getString(
                        R.string.payment_please_wait, loadingTimerState.countDownValue.toString()
                    )
            }
        }
    }

    private fun setRewardViewResources(drawableId: Int, subtitleText: String) {
        binding.apply {
            rewardImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), drawableId
                )
            )
            rewardSubtitleText.text = subtitleText
        }
    }

    private fun goToLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireContext())
    }

    override fun handleViewEvent(event: PaymentResultContract.ViewEvents) {
        when (event) {
            is PaymentResultContract.ViewEvents.OpenWhatsAppPromotionShare -> openWhatsAppToShareScreenshot(event.intent)
            is PaymentResultContract.ViewEvents.GoToLogin -> {
                goToLogin()
            }
            is PaymentResultContract.ViewEvents.GoToClaimRewardScreen ->
                goToClaimRewardScreen(event.reward, getCurrentState().juspayPaymentPollingModel?.paymentId ?: "")
            is PaymentResultContract.ViewEvents.ShowToast -> shortToast(event.msg)
            is PaymentResultContract.ViewEvents.CopyTxnIdToClipBoard -> copyTxnIdTOClipBoard(event.txnId)
            is PaymentResultContract.ViewEvents.TakeScreenShot -> takeScreenShot(event.shareLink)
            is PaymentResultContract.ViewEvents.RetryPayment -> {
                paymentListener?.onRetryClicked()
            }
            is PaymentResultContract.ViewEvents.NetworkError -> {
                paymentListener?.onNetworkError()
            }
            PaymentResultContract.ViewEvents.OtherError -> {
                paymentListener?.onOtherError()
            }
            PaymentResultContract.ViewEvents.CallCustomerCare -> callSupport()
            is PaymentResultContract.ViewEvents.SendWhatsAppMessage -> startActivity(event.intent)
            PaymentResultContract.ViewEvents.ShowDefaultError -> shortToast(getString(R.string.payment_something_went_wrong))
            PaymentResultContract.ViewEvents.ShowWhatsAppError -> shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportNumber))
        startActivity(intent)
    }

    override fun loadIntent(): UserIntent {
        return PaymentResultContract.Intent.Load
    }
}
