package `in`.okcredit.collection_ui.ui.kyc

import `in`.okcredit.analytics.Event
import `in`.okcredit.collection.contract.KycDialogListener
import `in`.okcredit.collection.contract.KycDialogMode
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import `in`.okcredit.shared.databinding.DialogTemplate1Binding
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible

class KycDialog : ExpandedBottomSheetDialogFragment() {

    private val binding: DialogTemplate1Binding by viewLifecycleScoped(DialogTemplate1Binding::bind)
    private var listener: KycDialogListener? = null

    private var isReminderFlow = false
    private var isLimitReached = false
    private var doNotAskAgain = false
    private var kycRisk = KycRiskCategory.NO_RISK
    private var kycStatus = KycStatus.NOT_SET
    private var shouldShowCreditCardInfoForKyc = false

    private var mode: KycDialogMode? = null

    companion object {
        const val TAG = "KycDialog"
        private const val KYC_MODE = "kyc_mode"
        private const val KYC_STATUS = "kyc_status"
        private const val KYC_RISK = "kyc_risk"
        private const val SHOULD_SHOW_CREDIT_CARD_INFO_FOR_KYC = "should_show_credit_card_info_for_kyc"
        private const val IS_REMINDER_FLOW = "is_reminder_flow"
        private const val IS_LIMIT_REACHED = "is_limit_reached"

        private const val START_KYC = "start_kyc"
        private const val LIMIT_REACHED = "limit_reached"
        private const val KYC_VERIFICATION = "kyc_verification"

        const val EVENT_KYC_VERIFICATION_DISMISSED = "kyc_verification_dismissed"
        const val EVENT_KYC_VERIFICATION_SKIPPED = "kyc_verification_skipped"
        const val EVENT_KYC_VERIFICATION_STARTED = "kyc_verification_started"
        const val EVENT_REDO_KYC_CLICKED = "redo_kyc_clicked"
        const val EVENT_IN_APP_NOTIFICATION_DISPLAYED = Event.IN_APP_NOTI_DISPLAYED

        fun newInstance(
            mode: KycDialogMode,
            kycStatus: KycStatus?,
            kycRisk: KycRiskCategory?,
            shouldShowCreditCardInfoForKyc: Boolean,
        ): KycDialog {
            val fragment = KycDialog()
            val bundle = Bundle()
            bundle.putString(KYC_MODE, mode.value)
            bundle.putString(KYC_STATUS, kycStatus?.value)
            bundle.putString(KYC_RISK, kycRisk?.value)
            bundle.putBoolean(SHOULD_SHOW_CREDIT_CARD_INFO_FOR_KYC, shouldShowCreditCardInfoForKyc)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    fun setListener(listener: KycDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogTemplate1Binding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        loadDataFromBundle()
        render()
    }

    private fun setListeners() {
        binding.action1.setOnClickListener {
            listener?.onCancelKyc(doNotAskAgain, EVENT_KYC_VERIFICATION_SKIPPED)
            dismiss()
        }
        binding.action2.setOnClickListener {
            if (kycStatus != KycStatus.COMPLETE && kycStatus != KycStatus.PENDING) {
                val eventName = if (kycStatus == KycStatus.FAILED) {
                    EVENT_REDO_KYC_CLICKED
                } else {
                    EVENT_KYC_VERIFICATION_STARTED
                }
                listener?.onConfirmKyc(doNotAskAgain, eventName)
            } else {
                listener?.onConfirmKyc(doNotAskAgain)
            }
            dismiss()
        }
    }

    private fun loadDataFromBundle() {
        kycStatus = KycStatus.valueOf(arguments?.getString(KYC_STATUS) ?: KycStatus.NOT_SET.value)
        kycRisk = KycRiskCategory.valueOf(arguments?.getString(KYC_RISK) ?: KycRiskCategory.NO_RISK.value)
        shouldShowCreditCardInfoForKyc = arguments?.getBoolean(SHOULD_SHOW_CREDIT_CARD_INFO_FOR_KYC) ?: false
        isReminderFlow = arguments?.getBoolean(IS_REMINDER_FLOW) ?: false
        isLimitReached = arguments?.getBoolean(IS_LIMIT_REACHED) ?: false
        val kycMode = arguments?.getString(KYC_MODE)
        if (kycMode.isNullOrEmpty().not()) {
            mode = KycDialogMode.valueOf(kycMode!!)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onDismissKyc(EVENT_KYC_VERIFICATION_DISMISSED)
    }

    private fun render() {
        when (mode) {
            KycDialogMode.Complete -> showKycComplete()
            KycDialogMode.Risk -> showRisk()
            KycDialogMode.Status -> showStatus()
            KycDialogMode.Remind -> showRemind()
        }
    }

    private fun showRisk() {
        when (kycRisk) {
            KycRiskCategory.LOW -> showKycRiskLow()
            KycRiskCategory.HIGH -> showKycRiskHigh()
            else -> {
                // do nothing
            }
        }

        listener?.onDisplayed(EVENT_IN_APP_NOTIFICATION_DISPLAYED, LIMIT_REACHED)
    }

    private fun showStatus() {
        when (kycStatus) {
            KycStatus.COMPLETE -> {
                showKycSuccess()
            }
            KycStatus.PENDING -> {
                showKycPending()
            }
            KycStatus.FAILED -> {
                showKycFailed()
            }
        }

        listener?.onDisplayed(EVENT_IN_APP_NOTIFICATION_DISPLAYED, KYC_VERIFICATION)
    }

    private fun showRemind() {
        showRisk()
        binding.dontAskAgain.visible()
        binding.dontAskAgain.buttonTintList = ColorStateList.valueOf(getColorCompat(R.color.green_primary))
        binding.dontAskAgain.setOnCheckedChangeListener { buttonView, isChecked ->
            doNotAskAgain = isChecked
        }
        binding.action1.text = getString(R.string.skip_and_send)
    }

    private fun showKycFailed() {
        binding.title.text = getString(R.string.kyc_failed)
        binding.description.text = getString(R.string.kyc_failed_description)
        binding.img.setImageDrawable(getDrawableCompact(R.drawable.ic_kyc_risk))
        binding.action1.text = getString(R.string.not_now)
        binding.action2.text = getString(R.string.redo_kyc)
    }

    private fun showKycSuccess() {
        binding.title.text = getString(R.string.kyc_success)
        val description = SpannableStringBuilder(Html.fromHtml(getString(R.string.kyc_success_description)))
        if (kycRisk == KycRiskCategory.LOW && shouldShowCreditCardInfoForKyc) {
            description.append(Html.fromHtml(getString(R.string.kyc_success_credit_card_info)))
        }
        binding.description.text = description
        binding.img.setImageDrawable(getDrawableCompact(R.drawable.ic_kyc_success))
        binding.action2.text = getString(R.string.ok)
        binding.action1.gone()
    }

    private fun showKycPending() {
        binding.title.text = getString(R.string.kyc_pending)
        binding.description.text = getString(R.string.kyc_pending_description)
        binding.img.setImageDrawable(getDrawableCompact(R.drawable.ic_kyc_pending))
        binding.action2.text = getString(R.string.ok)
        binding.action1.gone()
    }

    private fun showKycComplete() {
        binding.title.text = getString(R.string.complete_kyc)
        val description =
            SpannableStringBuilder(getString(R.string.complete_kyc_description))
        val startIndex = description.indexOf("₹")
        val endIndex = startIndex + 7
        description.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green_primary
                )
            ),
            startIndex,
            endIndex,
            0
        )
        binding.img.setImageDrawable(getDrawableCompact(R.drawable.ic_kyc_illusration))
        binding.description.text = description
        binding.action2.text = getString(R.string.start_kyc)

        listener?.onDisplayed(EVENT_IN_APP_NOTIFICATION_DISPLAYED, START_KYC)
    }

    private fun showKycRiskLow() {
        binding.title.text = getString(R.string.kyc_limit_reached)
        binding.description.text = when (kycStatus) {
            KycStatus.COMPLETE -> getString(R.string.kyc_limit_reached_kyc_done)
            KycStatus.PENDING -> getString(R.string.kyc_limit_reached_low_risk_kyc_status_pending)
            else -> getString(R.string.kyc_limit_reached_low_risk_kyc_not_done)
        }
        binding.img.setImageDrawable(getDrawableCompact(R.drawable.ic_kyc_risk))
        if (kycStatus == KycStatus.COMPLETE || kycStatus == KycStatus.PENDING) {
            binding.action2.text = getString(R.string.done)
            binding.action1.gone()
        } else if (kycStatus == KycStatus.FAILED) {
            binding.action2.text = getString(R.string.redo_kyc)
        } else {
            binding.action2.text = getString(R.string.start_kyc)
        }
    }

    private fun showKycRiskHigh() {
        binding.title.text = getString(R.string.kyc_limit_reached)
        binding.description.text = when (kycStatus) {
            KycStatus.COMPLETE -> getString(R.string.kyc_limit_reached_kyc_done)
            KycStatus.PENDING -> getString(R.string.kyc_limit_reached_high_risk_kyc_status_pending)
            else -> getString(R.string.kyc_limit_reached_high_risk_kyc_not_done)
        }
        binding.img.setImageDrawable(getDrawableCompact(R.drawable.ic_kyc_risk))
        if (kycStatus == KycStatus.COMPLETE || kycStatus == KycStatus.PENDING) {
            binding.action2.text = getString(R.string.done)
            binding.action1.gone()
        } else {
            binding.action2.text = getString(R.string.start_kyc)
        }
    }
}
