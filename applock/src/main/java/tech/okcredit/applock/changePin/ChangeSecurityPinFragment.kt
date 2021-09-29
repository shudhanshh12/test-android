package tech.okcredit.applock.changePin

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.SmsHelper
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.android.base.utils.staging
import tech.okcredit.applock.R
import tech.okcredit.applock.analytics.AppLockEventProperties
import tech.okcredit.applock.analytics.AppLockEventProperties.CONFIRM_OTP_FAILURE
import tech.okcredit.applock.analytics.AppLockEventProperties.CONFIRM_OTP_SUCCESSFUL
import tech.okcredit.applock.analytics.AppLockEventTracker
import tech.okcredit.applock.changePin.ChangeSecurityPinContract.*
import tech.okcredit.applock.databinding.ChangeSecurityPinBinding
import tech.okcredit.applock.databinding.OtpLayoutBinding
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChangeSecurityPinFragment : BaseFragment<State, ViewEvent, Intent>(
    "ChangeSecurityPinScreen", R.layout.change_security_pin
) {
    companion object {
        const val STAGING_OTP = "000000"
        private const val OTP_LENGTH = 6
        const val Source = "Source"
    }

    @Inject
    internal lateinit var smsHelper: Lazy<SmsHelper>

    @Inject
    lateinit var appLockEventTracker: Lazy<AppLockEventTracker>

    private var errorSnackbar: Snackbar? = null
    private val binding: ChangeSecurityPinBinding by viewLifecycleScoped(ChangeSecurityPinBinding::bind)
    private val otpBinding: OtpLayoutBinding by viewLifecycleScoped(OtpLayoutBinding::bind)

    private var isOTPAuthEventTriggered = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.disableScreanCapture()
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun trackOtpAuthStarted() {
        if (isOTPAuthEventTriggered) return
        activity?.intent?.let {
            appLockEventTracker.get().trackEvents(
                eventName = AppLockEventProperties.OTP_AUTHENTICATION_STARTED,
                flow = it.getStringExtra(Source)
            )
            isOTPAuthEventTriggered = true
        }
    }

    private fun initViews() {
        otpBinding.apply {
            incompleteOtpAnimation.repeatCount = LottieDrawable.INFINITE
            otp.setOtpCompletionListener {
                onOtpEntered(it)
            }
            otp.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    executeIfFragmentViewAvailable {
                        if (isOtpComplete(s)) {
                            sms.setImageResource(R.drawable.complete_otp)
                            incompleteOtpAnimation.pauseAnimation()
                            incompleteOtpAnimation.gone()
                        } else {
                            sms.setImageResource(R.drawable.incomplete_otp)
                            if (!incompleteOtpAnimation.isAnimating) {
                                incompleteOtpAnimation.playAnimation()
                            }
                            incompleteOtpAnimation.visible()
                            resetOtpViews()
                            hideSnackbar()
                        }
                    }
                }
            })
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(dispatcherProvider.get().main()) {
                    delay(10000)
                    resendOtp.visible()
                }
            }
        }
    }

    internal fun hideSnackbar() {
        errorSnackbar?.dismiss()
    }

    private fun updateSubtitle(state: State) {
        binding.apply {
            when (state.otpSent) {
                true -> {
                    otpBinding.subtitle.text = getString(R.string.otp_sent_message, state.mobile)
                    otpBinding.subtitle.visible()
                    enableOtpView(true)
                }
                false -> {
                    otpBinding.subtitle.text = getString(state.errorMessage ?: R.string.send_otp_failure)
                    otpBinding.subtitle.visible()
                    enableOtpView(false)
                }
                else -> {
                    // In progress
                    otpBinding.subtitle.gone()
                    enableOtpView(false)
                }
            }
        }
    }

    internal fun resetOtpViews() {
        otpBinding.apply {
            otp.setTextColor(resources.getColor(R.color.green_primary))
            otp.setLineColor(resources.getColor(R.color.black))
            incorrectOtp.gone()
        }
    }

    internal fun isOtpComplete(otp: Editable?) = otp?.length == OTP_LENGTH

    private fun onOtpEntered(otp: String) {
        pushIntent(Intent.VerifyOtp(otp))
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            otpBinding.resendOtp.clicks().map {
                otpBinding.otp.clear()
                appLockEventTracker.get().trackEvents(
                    eventName = AppLockEventProperties.RESEND_OTP,
                    flow = getCurrentState().source
                )
                Intent.ResendOtp
            }
        )
    }

    private fun initSmsRetriever() {
        smsHelper.get().startListening().observe(
            viewLifecycleOwner,
            Observer {
                onOtpEntered(it)
                otpBinding.otp.setText(it)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        initSmsRetriever()
        trackOtpAuthStarted()
    }

    override fun onPause() {
        super.onPause()
        smsHelper.get().stopListening()
    }

    override fun render(state: State) {
        updateSubtitle(state)
        updateVerificationViews(state)
        updateOtpVerificationStatus(state)
        setToolbarTitle(state)
    }

    private fun setToolbarTitle(state: State) {
        if (state.isUpdatePassword) {
            binding.tvToolbarTitle.text = getString(R.string.change_security_pin)
        } else {
            binding.tvToolbarTitle.text = getString(R.string.set_security_pin)
        }
    }

    private fun updateOtpVerificationStatus(state: State) {
        if (state.incorrectOtp) {
            showIncorrectOtpError(state)
        }
    }

    private fun updateVerificationViews(state: State) {
        if (state.verificationInProgress) {
            showProgressBar()
        } else {
            hideProgressBar()
        }
    }

    private fun showProgressBar() {
        disableInteractions()
        otpBinding.progressBar.visible()
    }

    private fun hideProgressBar() {
        enableInteractions()
        otpBinding.progressBar.gone()
    }

    private fun disableInteractions() {
        binding.apply {
            otpBinding.resendOtp.isEnabled = false
        }
    }

    private fun enableInteractions() {
        binding.apply {
            otpBinding.resendOtp.isEnabled = true
        }
    }

    private fun enableOtpView(enable: Boolean) {
        if (enable) {
            otpBinding.apply {
                otp.isFocusableInTouchMode = true
                otp.isCursorVisible = true
                otp.isEnabled = true
            }
        } else {
            otpBinding.apply {
                otp.isFocusableInTouchMode = false
                otp.isEnabled = false
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.SendOtp -> {
                Observable.timer(500, TimeUnit.MILLISECONDS)
                    .subscribe {
                        pushIntent(Intent.SendOtpClick)
                    }
            }
            is ViewEvent.SendOtpSuccess -> {
                view?.snackbar(
                    getString(R.string.send_otp_success),
                    Snackbar.LENGTH_SHORT
                )?.show()
                fillOtpForStaging()
            }
            is ViewEvent.Toast -> {
                hideSoftKeyboard()
                view?.snackbar(getString(event.resId), Snackbar.LENGTH_SHORT)?.show()
            }
            is ViewEvent.SendOtpError -> showSendOtpError(event.errorMessage)

            is ViewEvent.GoToSetPinScreen -> {
                appLockEventTracker.get()
                    .trackEvents(
                        eventName = CONFIRM_OTP_SUCCESSFUL, flow = getCurrentState().source
                    )
                requireActivity().runOnUiThread {
                    val actionData = ChangeSecurityPinFragmentDirections.actionChangePinScreenToSetPinScreen()
                    actionData.source = getCurrentState().source
                    NavHostFragment.findNavController(this).navigate(actionData)
                }
            }
        }
    }

    private fun fillOtpForStaging() {
        staging { otpBinding.otp.setText(STAGING_OTP) }
    }

    private fun showSendOtpError(@StringRes errorMessage: Int) {
        if (KeyboardVisibilityEvent.isKeyboardVisible(requireActivity()))
            hideSoftKeyboard()
        otpBinding.resendOtp.visible()
        errorSnackbar = view?.snackbar(
            getString(errorMessage),
            Snackbar.LENGTH_INDEFINITE
        )?.setAction(getString(R.string.retry)) {
            pushIntent(Intent.ResendOtp)
        }
        errorSnackbar?.show()
    }

    private fun showIncorrectOtpError(state: State) {
        appLockEventTracker.get()
            .trackEvents(
                eventName = CONFIRM_OTP_FAILURE, flow = state.source
            )
        otpBinding.apply {
            sms.setImageResource(R.drawable.invalid_otp)
            otp.setTextColor(resources.getColor(R.color.red_1))
            otp.setLineColor(resources.getColor(R.color.red_1))
            state.errorMessage?.let {
                incorrectOtp.setText(it)
                incorrectOtp.visible()
            }
            resendOtp.visible()
            hideProgressBar()
            showSoftKeyboard(otp)
        }
    }

    override fun onBackPressed(): Boolean {
        findNavController().navigateUp()
        return super.onBackPressed()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }
}
