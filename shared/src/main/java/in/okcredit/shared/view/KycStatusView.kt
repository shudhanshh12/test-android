package `in`.okcredit.shared.view

import `in`.okcredit.shared.R
import `in`.okcredit.shared.databinding.ViewKycStatusBinding
import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import tech.okcredit.android.base.extensions.getColorDrawable
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.withClickableSpan
import java.util.*

class KycStatusView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    companion object {
        const val COMPLETE = "complete"
        const val PENDING = "pending"
        const val FAILED = "failed"
        const val HIGH = "high"
        const val LOW = "low"

        const val EVENT_KYC_VERIFICATION_DISMISSED = "kyc_verification_dismissed"
        const val EVENT_KYC_VERIFICATION_STARTED = "kyc_verification_started"
        const val EVENT_REDO_KYC_CLICKED = "redo_kyc_clicked"
    }

    private var binding: ViewKycStatusBinding = ViewKycStatusBinding.inflate(LayoutInflater.from(ctx), this)

    private var mListener: Listener? = null

    init {
        background = getColorDrawable(R.color.orange_lite)
    }

    interface Listener {
        fun onBannerDisplayed(bannerType: String)
        fun onStartKyc(eventName: String)
        fun onClose(eventName: String)
    }

    fun setData(kycStatus: String, kycRiskCategory: String, isLimitReached: Boolean, canShowBorder: Boolean = false) {
        processData(
            kycStatus = kycStatus.lowercase(Locale.getDefault()),
            kycRiskCategory = kycRiskCategory.lowercase(Locale.getDefault()),
            isLimitReached = isLimitReached,
            canShowBorder = canShowBorder
        )
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    fun showKycBanner() {
        render(
            getString(R.string.kyc_banner_description),
            R.color.indigo_lite,
            R.color.indigo_lite_1,
            true,
            canShowBorder = false,
            bannerType = "start_kyc"
        )
    }

    fun hideCloseButton() {
        binding.close.gone()
    }

    private fun processData(
        kycStatus: String,
        kycRiskCategory: String,
        isLimitReached: Boolean,
        canShowBorder: Boolean,
    ) {
        if (isLimitReached) {
            val background = if (canShowBorder) R.drawable.kyc_limit_reached_background else R.color.red_lite
            val buttonBackground = R.color.red_lite_1
            var canStartKyc = false
            val text = when (kycStatus) {
                COMPLETE -> getKycCompleteText()
                PENDING -> getKycPendingText(kycRiskCategory)
                else -> {
                    canStartKyc = true
                    getKycNotDoneText(kycRiskCategory)
                }
            }
            render(
                text = text,
                background = background,
                buttonBackground = buttonBackground,
                canStartKyc = canStartKyc,
                isKycFailed = kycStatus == FAILED,
                canShowBorder = canShowBorder,
                bannerType = "limit_reached"
            )
        } else {
            val failedBackground = if (canShowBorder) R.drawable.kyc_limit_reached_background else R.color.red_lite
            val pendingBackground = if (canShowBorder) R.drawable.kyc_pending_background else R.color.orange_lite
            when (kycStatus) {
                FAILED -> render(
                    text = getString(R.string.kyc_failed_description),
                    background = failedBackground,
                    buttonBackground = R.color.red_lite_1,
                    canStartKyc = true,
                    isKycFailed = true,
                    canShowBorder = canShowBorder,
                    bannerType = "kyc_status"
                )
                PENDING -> render(
                    text = "${getString(R.string.kyc_pending)}. ${getString(R.string.kyc_pending_description)}",
                    background = pendingBackground,
                    buttonBackground = R.color.orange_lite_1,
                    canStartKyc = false,
                    canShowBorder = canShowBorder,
                    bannerType = "kyc_status"
                )
                else -> {
                    gone()
                }
            }
        }
    }

    private fun getKycCompleteText() =
        "${getString(R.string.kyc_limit_reached)} ${getString(R.string.kyc_limit_reached_kyc_done)}"

    private fun getKycPendingText(kycRiskCategory: String) = if (kycRiskCategory == HIGH) {
        "${getString(R.string.kyc_limit_reached)} ${getString(R.string.kyc_limit_reached_high_risk_kyc_status_pending)}"
    } else {
        "${getString(R.string.kyc_limit_reached)} ${getString(R.string.kyc_limit_reached_low_risk_kyc_status_pending)}"
    }

    private fun getKycNotDoneText(kycRiskCategory: String) = if (kycRiskCategory == HIGH) {
        "${getString(R.string.kyc_limit_reached)} ${getString(R.string.kyc_limit_reached_high_risk_kyc_not_done)}"
    } else {
        "${getString(R.string.kyc_limit_reached)} ${getString(R.string.kyc_limit_reached_low_risk_kyc_not_done)}"
    }

    private fun render(
        text: String,
        background: Int,
        buttonBackground: Int,
        canStartKyc: Boolean,
        isKycFailed: Boolean = false,
        canShowBorder: Boolean,
        bannerType: String,
    ) {
        val content = SpannableStringBuilder(text)
        val startKyc = if (isKycFailed)
            SpannableStringBuilder(getString(R.string.redo_kyc))
        else SpannableStringBuilder(getString(R.string.start_kyc))
        startKyc.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.indigo_primary)), 0, startKyc.length, 0)
        startKyc.withClickableSpan(0, startKyc.length) {
            if (isKycFailed) {
                mListener?.onStartKyc(EVENT_REDO_KYC_CLICKED)
            } else {
                mListener?.onStartKyc(EVENT_KYC_VERIFICATION_STARTED)
            }
        }
        binding.kycMessage.text = if (canStartKyc) content.append(" ").append(startKyc) else content
        binding.kycMessage.movementMethod = LinkMovementMethod.getInstance()
        binding.root.background = if (canShowBorder)
            ContextCompat.getDrawable(context, background) else getColorDrawable(background)
        binding.close.backgroundTintList = ColorStateList.valueOf(resources.getColor(buttonBackground))
        binding.close.setOnClickListener {
            gone()
            mListener?.onClose(EVENT_KYC_VERIFICATION_DISMISSED)
        }
        mListener?.onBannerDisplayed(bannerType)
    }
}
