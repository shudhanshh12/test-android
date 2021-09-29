package `in`.okcredit.payment.ui.payment_blind_pay

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.payment.R
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.databinding.FragmentPaymentBlindPayBinding
import `in`.okcredit.payment.utils.formatDecimalString
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_payment_blind_pay.*
import merchant.okcredit.accounting.contract.model.SupportType
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.InputFilterDecimal
import tech.okcredit.app_contract.LegacyNavigator
import java.util.*
import javax.inject.Inject

class PaymentBlindPayFragment :
    BaseFragment<PaymentBlindPayContract.State,
        PaymentBlindPayContract.ViewEvents,
        PaymentBlindPayContract.Intent>("PaymentBlindPayFragment") {

    @Inject
    lateinit var paymentNavigator: Lazy<PaymentNavigator>

    @Inject
    lateinit var paymentAnalyticsEvents: Lazy<PaymentAnalyticsEvents>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private var isAmountPrefilled = false

    private var isAmountEdited = false

    private val binding: FragmentPaymentBlindPayBinding by viewLifecycleScoped(FragmentPaymentBlindPayBinding::bind)

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent {
        return PaymentBlindPayContract.Intent.Load
    }

    override fun render(state: PaymentBlindPayContract.State) {
        setToolbarInfo(state)
        preFillAmountUi(state)
        setLimitReachedUi(state)
        setUpEducationData()
        setCustomerBannerUi(state.supportType)
    }

    private fun setCustomerBannerUi(supportType: SupportType) {
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

    private fun setToolbarInfo(state: PaymentBlindPayContract.State) {
        binding.screenTitle.text = state.profileName
        if (state.profileName.isEmpty()) return

        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                state.profileName.substring(0, 1).uppercase(Locale.getDefault()),
                ColorGenerator.MATERIAL.getColor(state.profileName)
            )

        GlideApp
            .with(this)
            .load(state.profileImage)
            .circleCrop()
            .placeholder(defaultPic)
            .fallback(defaultPic)
            .into(binding.profileImage)
    }

    private fun setUpEducationData() {
        if (getCurrentState().isSupplier()) {
            tvDepositSubHeader.text =
                getString(
                    R.string.blind_pay_education_deposit_sub_header_supplier
                )
            tvRefundSubHeader.text =
                getString(
                    R.string.blind_pay_education_refund_sub_header_supplier
                )

            tvReceiverHeader.text =
                getString(
                    R.string.blind_pay_education_receiver_header_supplier
                )
            tvReceiverSubHeader.text =
                getString(
                    R.string.blind_pay_education_receiver_sub_header_supplier
                )

            tvSettlementSubHeader.text =
                getString(
                    R.string.blind_pay_education_settlement_sub_header_supplier
                )
        } else {
            tvDepositSubHeader.text =
                getString(
                    R.string.blind_pay_education_deposit_sub_header_customer
                )
            tvReceiverHeader.text =
                getString(
                    R.string.blind_pay_education_receiver_header_customer
                )
            tvReceiverSubHeader.text =
                getString(
                    R.string.blind_pay_education_receiver_sub_header_customer
                )

            tvSettlementSubHeader.text =
                getString(
                    R.string.blind_pay_education_settlement_sub_header_supplier
                )

            tvRefundSubHeader.text =
                getString(
                    R.string.blind_pay_education_refund_sub_header_customer
                )
        }
    }

    private fun setLimitReachedUi(state: PaymentBlindPayContract.State) {
        state.currentAmountSelected?.let {
            binding.apply {
                ifLet(state.maxDailyLimit, state.remainingDailyLimit) { maxAmount, remainingAmount ->
                    if (maxAmount != -1L && remainingAmount < state.currentAmountSelected) {
                        getCurrentState().let {
                            paymentAnalyticsEvents.get().trackPaymentLimitWarningDisplayed(
                                accountId = it.accountId,
                                relation = it.getRelationFrmAccountType(),
                                screen = PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_SUMMARY_SCREEN,
                                flow = PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION,
                                dueAmount = it.dueBalance.toString(),
                                userTxnLimit = it.maxDailyLimit.toString(),
                                availTxnLimit = it.remainingDailyLimit.toString(),
                                amount = state.currentAmountSelected.toString(),
                                preFilledAmount = it.getPaymentPrefillBalance().toString(),
                                easyPay = true,
                            )
                        }

                        setLimitReachedText(state)
                        tvRupeeIcon.setTextColor(getColorCompat(R.color.red_primary))
                        vwUnderLineAmount.setBackgroundColor(getColorCompat(R.color.red_primary))
                    } else {
                        tvRupeeIcon.setTextColor(getColorCompat(R.color.green_primary))
                        vwUnderLineAmount.setBackgroundColor(getColorCompat(R.color.green_primary))
                        binding.tvLimitReachedWarning.text =
                            getString(R.string.blind_pay_max_limit_education, state.maxDailyLimit.div(100).toString())

                        tvLimitReachedWarning.setTextColor(getColorCompat(R.color.green_primary))
                    }
                }
            }
        }
    }

    private fun setLimitReachedText(state: PaymentBlindPayContract.State) {
        val remainingLimitString = "₹${state.remainingDailyLimit.div(100)}"
        val limitReachedString =
            getString(
                R.string.payment_limit_reached,
                state.remainingDailyLimit.div(100).toString(),
                state.maxDailyLimit.div(100).toString()
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
            limitReachedString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvLimitReachedWarning.text = builder
        binding.tvLimitReachedWarning.setTextColor(getColorCompat(R.color.red_primary))
    }

    private fun preFillAmountUi(state: PaymentBlindPayContract.State) {
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

    override fun handleViewEvent(event: PaymentBlindPayContract.ViewEvents) {
        when (event) {
            is PaymentBlindPayContract.ViewEvents.ShowToast -> shortToast(event.msg)
            is PaymentBlindPayContract.ViewEvents.TrackPageSummaryEvent -> trackPageSummaryEvent(
                event.type,
                event.number
            )
            PaymentBlindPayContract.ViewEvents.CallCustomerCare -> callSupport()
            is PaymentBlindPayContract.ViewEvents.SendWhatsAppMessage -> startActivity(event.intent)
            PaymentBlindPayContract.ViewEvents.ShowDefaultError -> shortToast(getString(R.string.payment_something_went_wrong))
            PaymentBlindPayContract.ViewEvents.ShowWhatsAppError -> shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportNumber))
        startActivity(intent)
    }

    private fun trackPageSummaryEvent(type: String, number: String) {
        getCurrentState().let { state ->
            paymentAnalyticsEvents.get().trackPaymentSummaryPageViewed(
                accountId = state.accountId,
                dueAmount = state.dueBalance.toString(),
                userTxnLimit = state.maxDailyLimit.toString(),
                availTxnLimit = state.remainingDailyLimit.toString(),
                limitExhausted = state.maxDailyLimit != -1L && state.getPaymentPrefillBalance() > state.remainingDailyLimit,
                preFilledAmount = state.getPaymentPrefillBalance().toString(),
                riskValue = state.riskType,
                amount = if (!binding.etAmount.text.isNullOrEmpty()) "${
                binding.etAmount.text.toString().toLong()
                    .times(100)
                }" else "",
                relation = state.getRelationFrmAccountType(),
                easyPay = true,
                customerSupportType = type,
                customerSupportNumber = number,
                customerSupportMessage = getString(R.string.t_002_i_need_help_generic),
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentPaymentBlindPayBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        amountTextChangeWatcher()
        setClickListener()
    }

    private fun setClickListener() {
        makePayment.setOnClickListener {
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
                        easyPay = true,
                    )
                }

                updateUiOnProceed()
                openJuspaySdk()
            } else {
                if (!binding.etAmount.text.isNullOrEmpty() && binding.etAmount.text.toString()
                    .toDouble() < 1
                )
                    shortToast(R.string.payment_put_a_valid_amount_greater_than_1)
                else shortToast(R.string.payment_put_a_valid_amount)
            }
        }

        binding.buttonSupport.setOnClickListener {
            pushIntent(
                PaymentBlindPayContract.Intent.SupportClicked(
                    getString(R.string.t_002_i_need_help_generic),
                    getCurrentState().supportNumber
                )
            )
        }
    }

    private fun updateUiOnProceed() {
        binding.apply {
            etAmount.disable()
            makePayment.text = ""
            ivLoading.visible()
            ivLoading.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.payment_rotate
                )
            )
        }
    }

    private fun openJuspaySdk() {
        paymentNavigator.get().startJuspaySdk(
            requireActivity(),
            getCurrentState().linkId,
            getCurrentState().currentAmountSelected ?: 0L
        )
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
                            // we have to skip first edit event when we set prefilled amount (already logging thr) thn only will get actual edit event
                            if (isAmountPrefilled) {
                                isAmountEdited = true
                                trackAmountEntered(text.toDouble().times(100).toLong())
                            }
                        }
                    }
                }
                pushIntent(PaymentBlindPayContract.Intent.SetAmountEntered(text.toDouble().times(100).toLong()))
            }
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
                riskValue = it.riskType,
                easyPay = true,
            )
        }
    }
}
