package `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet

import `in`.okcredit.analytics.PropertyKey.PAYMENT
import `in`.okcredit.analytics.PropertyKey.SOURCE
import `in`.okcredit.collection.contract.CollectionDestinationType
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.payment.PaymentActivity
import `in`.okcredit.payment.R
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.databinding.PaymentEditAmountBottomSheetBinding
import `in`.okcredit.payment.usecases.KycBannerType
import `in`.okcredit.payment.utils.CurrencyUtil
import `in`.okcredit.payment.utils.formatDecimalString
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.web.WebExperiment
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.payment_result_fragment.*
import merchant.okcredit.accounting.contract.model.SupportType
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.InputFilterDecimal
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class PaymentEditAmountBottomSheet :
    BaseBottomSheetWithViewEvents<PaymentEditAmountContract.State, PaymentEditAmountContract.ViewEvents,
        PaymentEditAmountContract.Intent>(
        "PaymentEditAmountBottomSheet"
    ) {

    @Inject
    lateinit var paymentNavigator: Lazy<PaymentNavigator>

    @Inject
    lateinit var paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private var paymentEditAmountBottomSheetListener: PaymentEditAmountBottomSheetListener? = null

    private var isAmountPrefilled = false

    private var isAmountEdited = false

    private val binding: PaymentEditAmountBottomSheetBinding by viewLifecycleScoped(
        PaymentEditAmountBottomSheetBinding::bind
    )

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        requireActivity().window.disableScreanCapture()
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboard)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return PaymentEditAmountBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomSheetBehaviuor()
        setClickListener()
        amountTextChangeWatcher()
        handleBackPress()
        setPaymentEditAmountBottomSheetListener(requireActivity() as PaymentActivity)
    }

    private fun handleBackPress() {
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismissAllowingStateLoss()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }
    }

    private fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    private fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(view: View, state: Int) {
                // to stop dragging of bottom sheet
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    private fun setBottomSheetBehaviuor() {
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()
            disableDraggingInBottomSheet(behavior)
        }
    }

    private fun addDecimalFilter() {
        val inputFilters = arrayOfNulls<InputFilter>(binding.etAmount.filters.size + 1)
        for ((counter, inputFilter) in binding.etAmount.filters.withIndex()) {
            inputFilters[counter] = inputFilter
        }
        inputFilters[inputFilters.size - 1] = InputFilterDecimal(9, 2) {}
        binding.etAmount.filters = inputFilters
    }

    private fun amountTextChangeWatcher() {
        addDecimalFilter()
        binding.etAmount.afterTextChange { text ->
            if (text.length == 1 && text.startsWith("0")) {
                binding.etAmount.clear()
                return@afterTextChange
            }
            if (text.isNotEmpty() && text.toDouble() > 0.0) {
                if (isStateInitialized()) {
                    if (!isAmountEdited) {
                        getCurrentState().let {
                            // we have to skip first edit event when we set prefilled amount (already logging thr) thn
                            // only will get actual edit event
                            if (isAmountPrefilled) {
                                isAmountEdited = true
                                trackAmountEntered(text.toDouble().times(100).toLong())
                            }
                        }
                    }
                }
                pushIntent(PaymentEditAmountContract.Intent.SetAmountEntered(text.toDouble().times(100).toLong()))
            } else {
                pushIntent(PaymentEditAmountContract.Intent.SetAmountEntered(0))
            }
        }
    }

    private fun trackAmountEntered(amountInPaisa: Long) {
        getCurrentState().let {
            paymentAnalyticsEvents.get().trackPaymentAmountEntered(
                accountId = it.accountId,
                relation = it.getRelationFrmAccountType(),
                screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_SUMMARY_SCREEN,
                flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                dueAmount = it.dueBalance.toString(),
                userTxnLimit = it.maxDailyLimit.toString(),
                availTxnLimit = it.remainingDailyLimit.toString(),
                limitExhausted = it.maxDailyLimit != -1L && amountInPaisa > it.remainingDailyLimit,
                amount = amountInPaisa.toString(),
                preFilledAmount = it.getPaymentPrefillBalance().toString(),
                riskValue = it.riskType
            )
        }
    }

    private fun setClickListener() {
        binding.apply {
            mbProceed.setOnClickListener {
                if (isValidAmount()) {
                    getCurrentState().let {
                        paymentAnalyticsEvents.get().trackClickProceedPayment(
                            accountId = it.accountId,
                            relation = it.getRelationFrmAccountType(),
                            screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_ADDRESS_DETAILS,
                            dueAmount = it.dueBalance.toString(),
                            amount = binding.etAmount.text.toString().toDouble().times(100).toLong().toString(),
                            type = it.destinationType,
                            riskType = it.riskType,
                            totalLimit = it.maxDailyLimit,
                            limitLeft = it.remainingDailyLimit,
                        )
                    }
                    updateUiOnProceed()
                    openJuspaySdk()
                } else {
                    if (!binding.etAmount.text.isNullOrEmpty() && binding.etAmount.text.toString().toDouble() < 1) {
                        shortToast(R.string.payment_put_a_valid_amount_greater_than_1)
                    } else {
                        shortToast(R.string.payment_put_a_valid_amount)
                    }
                }
            }

            ivCross.setOnClickListener {
                dismissAllowingStateLoss()
            }

            ivEdit.setOnClickListener {
                getCurrentState().let {
                    paymentAnalyticsEvents.get().trackChangePaymentDetails(
                        accountId = it.accountId,
                        relation = it.getRelationFrmAccountType(),
                        screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_SUMMARY_SCREEN,
                        flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                        dueAmount = it.dueBalance.toString(),
                        type = it.destinationType
                    )
                }
                paymentEditAmountBottomSheetListener?.onEditDetailsClicked()
                dismissAllowingStateLoss()
            }

            closeBannerImageView.setOnClickListener {
                pushIntent(PaymentEditAmountContract.Intent.CloseKycInfoBanner)
            }

            buttonSupport.setOnClickListener {
                pushIntent(
                    PaymentEditAmountContract.Intent.SupportClicked(
                        getString(R.string.t_002_i_need_help_generic),
                        getCurrentState().supportNumber
                    )
                )
            }
        }
    }

    private fun isValidAmount(): Boolean {
        getCurrentState().let { state ->
            state.currentAmountSelected?.let {
                ifLet(state.maxDailyLimit, state.remainingDailyLimit) { _, remaining ->
                    return !binding.etAmount.text.isNullOrEmpty() && binding.etAmount.text.toString()
                        .toDouble() >= 1 && remaining >= it
                }
            }
        }
        return false
    }

    private fun updateUiOnProceed() {
        binding.apply {
            etAmount.disable()
            mbProceed.text = ""
            ivLoading.visible()
            ivEdit.gone()
            ivLoading.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.payment_rotate
                )
            )
        }
    }

    private fun openJuspaySdk() {
        getCurrentState().let {
            paymentNavigator.get().startJuspaySdk(
                requireActivity(),
                getCurrentState().linkId,
                it.currentAmountSelected ?: 0L
            )
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: PaymentEditAmountContract.State) {
        setProfileUi(state)
        preFillAmountUi(state)
        setupKycOrLimitReachedBanner(state)
        showCashbackMessageIfAvailable(state)
        setTopBannerUi(state.supportType)
    }

    private fun setTopBannerUi(supportType: SupportType) {
        when (supportType) {
            SupportType.CALL -> setSupportBannerUi(supportType)
            SupportType.CHAT -> setSupportBannerUi(supportType)
            SupportType.NONE -> {
                binding.apply {
                    viewTop.visible()
                    buttonSupport.gone()
                }
            }
        }
    }

    private fun setSupportBannerUi(supportType: SupportType) {
        binding.apply {
            viewTop.gone()
            buttonSupport.visible()

            buttonSupport.text = if (supportType == SupportType.CALL) {
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

            buttonSupport.icon = getDrawableCompact(
                if (supportType == SupportType.CALL)
                    R.drawable.ic_call_support_indigo
                else
                    R.drawable.ic_whatsapp_indigo
            )
        }
    }

    private fun showCashbackMessageIfAvailable(state: PaymentEditAmountContract.State) {
        if (state.cashbackMessage.isNotNullOrBlank()) {
            binding.cashbackMessageTextView.text = state.cashbackMessage
            binding.cashbackMessageTextView.visible()
        } else {
            binding.cashbackMessageTextView.gone()
        }
    }

    private fun preFillAmountUi(state: PaymentEditAmountContract.State) {
        state.getPaymentPrefillBalance().let {
            if (!isAmountPrefilled) {
                val amountInDecimal = it.formatDecimalString()
                binding.etAmount.setText(amountInDecimal)
                val amount = binding.etAmount.text.toString()
                if (amount.isNotEmpty()) {
                    binding.etAmount.setSelection(amount.length)
                }

                // keeping after setting prefilled amount so that first edit event can be skip in text watcher
                isAmountPrefilled = true
            }
        }
    }

    private fun setupKycOrLimitReachedBanner(state: PaymentEditAmountContract.State) {
        when (val kycBannerType = state.kycBannerType) {
            is KycBannerType.None -> setTypeNone()
            is KycBannerType.InformationWithKycEntryPoint ->
                setTypeInformationWithKycEntryPoint(
                    kycBannerType, state.shouldShowCreditCardInfoForKyc
                )
            is KycBannerType.LimitReachedWithKycEntryPoint -> setTypeLimitReachedWithKycEntryPoint(kycBannerType)
            is KycBannerType.LimitReachedWithoutKycEntryPoint -> setTypeLimitReachedWithoutKycEntryPoint(kycBannerType)
        }
    }

    private fun setTypeNone() = binding.apply {
        tvRupeeIcon.setTextColor(getColorCompat(R.color.green_primary))
        vwUnderLineAmount.setBackgroundColor(getColorCompat(R.color.green_primary))
        kycOrLimitReachedGroup.gone()
    }

    private fun setTypeInformationWithKycEntryPoint(
        banner: KycBannerType.InformationWithKycEntryPoint,
        shouldShowCreditCardInfoForKyc: Boolean,
    ) =
        binding.apply {
            tvRupeeIcon.setTextColor(getColorCompat(R.color.green_primary))
            vwUnderLineAmount.setBackgroundColor(getColorCompat(R.color.green_primary))

            kycOrLimitReachedImageView.setImageResource(R.drawable.kyc_ic_pan)
            kycOrLimitReachedBackground.background =
                getDrawableCompact(R.drawable.background_indigo_with_1dp_border_stroke)

            closeBannerImageView.visible()

            val kycIntroText = SpannableStringBuilder(
                getString(R.string.kyc_information_banner, CurrencyUtil.formatV2(banner.futureAmountLimit))
            )
            val startKyc = SpannableStringBuilder(getString(R.string.complete_kyc))
            startKyc.setSpan(
                ForegroundColorSpan(getColorCompat(R.color.indigo_primary)),
                0,
                startKyc.length,
                0
            )
            startKyc.withClickableSpan(0, startKyc.length) {
                pushIntent(PaymentEditAmountContract.Intent.KycEntryPointClicked)
            }

            if (banner.kycRiskCategory == KycRiskCategory.LOW && shouldShowCreditCardInfoForKyc) {
                val creditCardText = getString(R.string.kyc_information_banner_credit_card_text)
                kycIntroText.append(" ").append(creditCardText)
            }

            kycOrLimitReachedTextView.text = kycIntroText.append(" ").append(startKyc)
            kycOrLimitReachedTextView.movementMethod = LinkMovementMethod.getInstance()
            kycOrLimitReachedGroup.visible()
        }

    private fun setTypeLimitReached() = binding.apply {
        tvRupeeIcon.setTextColor(getColorCompat(R.color.red_primary))
        vwUnderLineAmount.setBackgroundColor(getColorCompat(R.color.red_primary))

        kycOrLimitReachedImageView.setImageResource(R.drawable.payment_ic_report_problem)
        kycOrLimitReachedBackground.background = getDrawableCompact(R.drawable.payment_bg_limit_reached_warning)

        getCurrentState().let {
            paymentAnalyticsEvents.get().trackPaymentLimitWarningDisplayed(
                accountId = it.accountId,
                relation = it.getRelationFrmAccountType(),
                screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_SUMMARY_SCREEN,
                flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                dueAmount = it.dueBalance.toString(),
                userTxnLimit = it.maxDailyLimit.toString(),
                availTxnLimit = it.remainingDailyLimit.toString(),
                amount = it.currentAmountSelected.toString(),
                preFilledAmount = it.getPaymentPrefillBalance().toString()
            )
        }
    }

    private fun setTypeLimitReachedWithKycEntryPoint(
        banner: KycBannerType.LimitReachedWithKycEntryPoint,
    ) = binding.apply {
        setTypeLimitReached()

        val limitReachedText = getLimitReachedText(
            banner.maxDailyLimit, banner.remainingDailyLimit, R.string.payment_limit_reached_with_kyc
        )

        val startKyc = SpannableStringBuilder(getString(R.string.complete_kyc))
        startKyc.setSpan(
            ForegroundColorSpan(getColorCompat(R.color.indigo_primary)),
            0,
            startKyc.length,
            0
        )
        startKyc.withClickableSpan(0, startKyc.length) {
            pushIntent(PaymentEditAmountContract.Intent.KycEntryPointClicked)
        }

        kycOrLimitReachedTextView.text = limitReachedText.append(" ").append(startKyc)
        kycOrLimitReachedTextView.movementMethod = LinkMovementMethod.getInstance()
        kycOrLimitReachedGroup.visible()
        closeBannerImageView.gone()
    }

    private fun setTypeLimitReachedWithoutKycEntryPoint(
        banner: KycBannerType.LimitReachedWithoutKycEntryPoint,
    ) = binding.apply {
        setTypeLimitReached()

        val limitReachedText = getLimitReachedText(
            banner.maxDailyLimit, banner.remainingDailyLimit, R.string.payment_limit_reached
        )
        kycOrLimitReachedTextView.text = limitReachedText
        kycOrLimitReachedGroup.visible()
        closeBannerImageView.gone()
    }

    private fun getLimitReachedText(
        maxDailyLimit: Long,
        remainingDailyLimit: Long,
        stringId: Int,
    ): SpannableStringBuilder {
        val remainingLimitString = "₹${remainingDailyLimit.div(100)}"
        val maxDailyLimitString = "₹${maxDailyLimit.div(100)}"
        val limitReachedString =
            getString(
                stringId,
                remainingDailyLimit.div(100).toString(),
                maxDailyLimit.div(100).toString()
            )
        val builder = SpannableStringBuilder(limitReachedString)
        val firstIndex = limitReachedString.indexOf("₹")
        val secondIndex = limitReachedString.indexOf("₹", firstIndex + 1)
        builder.setSpan(
            StyleSpan(Typeface.BOLD),
            firstIndex,
            firstIndex + remainingLimitString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            StyleSpan(Typeface.BOLD),
            secondIndex,
            secondIndex + maxDailyLimitString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return builder
    }

    private fun setProfileUi(state: PaymentEditAmountContract.State) {
        binding.apply {
            state.let {
                tvAccountName.text = it.name
                if (state.destinationUpdateAllowed.not()) ivEdit.gone()
                val array = it.paymentAddress.split("@").toTypedArray()
                when (it.destinationType) {
                    CollectionDestinationType.BANK.value -> {
                        if (array.isNotEmpty()) {
                            tvAccountId.text = array[0]
                            array.let { arr ->
                                if (arr.size > 1) {
                                    tvAccountIfsc.text = arr[1]
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

    private fun trackPageSummaryEvent(type: String, number: String) {
        getCurrentState().let { state ->
            paymentAnalyticsEvents.get().trackPaymentSummaryPageViewed(
                accountId = state.accountId,
                dueAmount = state.dueBalance.toString(),
                userTxnLimit = state.maxDailyLimit.toString(),
                availTxnLimit = state.remainingDailyLimit.toString(),
                limitExhausted = state.maxDailyLimit != -1L &&
                    state.getPaymentPrefillBalance() > state.remainingDailyLimit,
                preFilledAmount = state.getPaymentPrefillBalance().toString(),
                riskValue = state.riskType,
                amount = if (!binding.etAmount.text.isNullOrEmpty()) "${
                binding.etAmount.text.toString().toLong()
                    .times(100)
                }" else "",
                relation = state.getRelationFrmAccountType(),
                customerSupportType = type,
                customerSupportNumber = number,
                customerSupportMessage = getString(R.string.t_002_i_need_help_generic)
            )
        }
    }

    private fun goToKycScreen() {
        val queryParams = mapOf(SOURCE to PAYMENT)
        if (getCurrentState().getRelationFrmAccountType() == PaymentAnalyticsEvents.PaymentPropertyValue.SUPPLIER) {
            legacyNavigator.get().goWebExperimentScreen(
                requireContext(),
                WebExperiment.Experiment.KYC_SUPPLIER.type,
                queryParams
            )
        } else {
            legacyNavigator.get().goWebExperimentScreen(
                requireContext(),
                WebExperiment.Experiment.KYC.type,
                queryParams
            )
        }
        activity?.finish()
    }

    private fun goToLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireContext())
    }

    override fun handleViewEvent(event: PaymentEditAmountContract.ViewEvents) {
        when (event) {
            is PaymentEditAmountContract.ViewEvents.GoToLogin -> goToLogin()
            is PaymentEditAmountContract.ViewEvents.GoToKycWebScreen -> goToKycScreen()
            is PaymentEditAmountContract.ViewEvents.ShowToast -> shortToast(event.msg)
            is PaymentEditAmountContract.ViewEvents.TrackPageSummaryEvent ->
                trackPageSummaryEvent(event.type, event.number)
            PaymentEditAmountContract.ViewEvents.CallCustomerCare -> callSupport()
            is PaymentEditAmountContract.ViewEvents.SendWhatsAppMessage -> startActivity(event.intent)
            PaymentEditAmountContract.ViewEvents.ShowDefaultError ->
                shortToast(getString(R.string.payment_something_went_wrong))
            PaymentEditAmountContract.ViewEvents.ShowWhatsAppError ->
                shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportNumber))
        startActivity(intent)
    }

    private fun setPaymentEditAmountBottomSheetListener(listener: PaymentEditAmountBottomSheetListener) {
        paymentEditAmountBottomSheetListener = listener
    }

    override fun loadIntent(): UserIntent {
        return PaymentEditAmountContract.Intent.Load
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        paymentEditAmountBottomSheetListener?.onDismissEditAmountSheet()
    }

    interface PaymentEditAmountBottomSheetListener {
        fun onEditDetailsClicked()
        fun onDismissEditAmountSheet()
    }
}
