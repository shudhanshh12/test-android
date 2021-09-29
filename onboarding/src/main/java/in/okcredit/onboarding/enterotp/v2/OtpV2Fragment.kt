package `in`.okcredit.onboarding.enterotp.v2

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.databinding.OtpV2FragmentBinding
import `in`.okcredit.onboarding.enterotp.v2.OtpContractV2.State
import `in`.okcredit.onboarding.enterotp.v2.OtpContractV2.ViewEvent
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.utils.staging
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class OtpV2Fragment : BaseFragment<State, ViewEvent, OtpContractV2.Intent>(
    "OtpV2Fragment",
    R.layout.otp_v2_fragment
) {

    val args: OtpV2FragmentArgs by navArgs()
    private val binding: OtpV2FragmentBinding by viewLifecycleScoped(OtpV2FragmentBinding::bind)

    private var blockBackButton = false

    @Inject
    internal lateinit var smsHelper: Lazy<SmsHelper>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private var errorSnackbar: Snackbar? = null

    companion object {
        const val ANIMATION_DURATION = 1500L
        private const val STAGING_OTP = "000000"
        private const val OTP_LENGTH = 6
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.disableScreanCapture()

        initViews()
        initSmsRetriever()
    }

    private fun initViews() {
        binding.apply {
            mobile.text = args.mobile
            incompleteOtpAnimation.repeatCount = LottieDrawable.INFINITE
            otp.setOtpCompletionListener { onOtpEntered(it) }
            otp.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
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

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(dispatcherProvider.get().main()) {
                    delay(10000)
                    resendOtp.visible()
                }
            }
        }
    }

    private fun isOtpComplete(otp: Editable?) = otp?.length == OTP_LENGTH

    private fun initSmsRetriever() {
        smsHelper.get().startListening().observe(
            viewLifecycleOwner,
            Observer {
                binding.otp.setText(it)
                onOtpEntered(it)
            }
        )
    }

    private fun fillOtpForStaging() {
        staging { binding.otp.setText(STAGING_OTP) }
    }

    override fun loadIntent(): UserIntent {
        return OtpContractV2.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.resendOtp.clicks().map {
                binding.otp.clear()
                hideSnackbar()
                OtpContractV2.Intent.ResendOtp
            },
            binding.mobile.clicks().map { OtpContractV2.Intent.EditMobile }
        )
    }

    override fun render(state: State) {
        updateSubtitle(state)
        updateOtpVerificationStatus(state)
        updateVerificationViews(state)
    }

    private fun updateSubtitle(state: State) {
        binding.apply {
            when (state.otpSent) {
                true -> {
                    subtitle.text = getString(R.string.otp_sent_message, args.mobile)
                    subtitle.visible()
                    enableOtpView(true)
                }
                false -> {
                    subtitle.text = getString(state.errorMessage ?: R.string.send_otp_failure)
                    subtitle.visible()
                    enableOtpView(false)
                }
                else -> {
                    // In progress
                    subtitle.gone()
                    enableOtpView(false)
                }
            }
        }
    }

    private fun updateOtpVerificationStatus(state: State) {
        if (state.incorrectOtp) {
            showIncorrectOtpError(state)
        }
    }

    private fun showIncorrectOtpError(state: State) {
        binding.apply {
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

    private fun updateVerificationViews(state: State) {
        if (state.verificationInProgress) {
            showProgressBar()
        } else {
            hideProgressBar()
        }
    }

    private fun showProgressBar() {
        disableInteractions()
        binding.progressBar.visible()
    }

    private fun hideProgressBar() {
        enableInteractions()
        binding.progressBar.gone()
    }

    private fun resetOtpViews() {
        binding.apply {
            otp.setTextColor(resources.getColor(R.color.green_primary))
            otp.setLineColor(resources.getColor(R.color.black))
            incorrectOtp.gone()
        }
    }

    private fun showSendOtpError(@StringRes errorMessage: Int) {
        binding.resendOtp.visible()
        errorSnackbar = view?.snackbar(
            getString(errorMessage),
            Snackbar.LENGTH_INDEFINITE
        )?.setAction(getString(R.string.retry)) {
            pushIntent(OtpContractV2.Intent.ResendOtp)
        }
        errorSnackbar?.show()
    }

    private fun showVerifyNetworkError(@StringRes errorMessage: Int) {
        hideSoftKeyboard()
        errorSnackbar = view?.snackbar(
            getString(errorMessage), Snackbar.LENGTH_INDEFINITE
        )?.setAction(getString(R.string.retry)) {
            hideSnackbar()
            onOtpEntered(binding.otp.text.toString())
        }
        errorSnackbar?.show()
    }

    private fun hideSnackbar() {
        errorSnackbar?.dismiss()
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.GoToSyncDataScreen -> {
                hideProgressBar()
                showSuccessAnimation { legacyNavigator.get().goToSyncScreen(requireActivity()) }
            }
            ViewEvent.GoToAppLockScreen -> {
                hideProgressBar()
                legacyNavigator.get().goToSystemAppLockScreenFromLogin(requireActivity())
                activity?.finish()
            }
            ViewEvent.GoToNameScreen -> {
                hideProgressBar()
                showSuccessAnimation { findNavController().navigate(OtpV2FragmentDirections.businessNameScreen()) }
            }
            ViewEvent.GoToMobileScreen -> {
                hideProgressBar()
                activity?.onBackPressed()
            }
            is ViewEvent.SendOtpSuccess -> {
                fillOtpForStaging()
                shortToast(R.string.send_otp_success)
            }
            is ViewEvent.SendOtpError -> showSendOtpError(event.errorMessage)
            is ViewEvent.VerifyNetworkError -> showVerifyNetworkError(event.errorMessage)
            is ViewEvent.Toast -> shortToast(event.resId)
        }
    }

    private fun showSuccessAnimation(block: () -> Unit) {
        hideSoftKeyboard()
        blockBackButton = true
        binding.apply {
            successViews.visible()
            successAnimation.playAnimation()
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(dispatcherProvider.get().main()) {
                    delay(ANIMATION_DURATION)
                    block()
                    hideSoftKeyboard()
                }
            }
        }
    }

    private fun onOtpEntered(otp: String, isAutoRead: Boolean = false) {
        pushIntent(OtpContractV2.Intent.VerifyOtp(otp, isAutoRead))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        smsHelper.get().stopListening()
    }

    override fun onPause() {
        super.onPause()
        hideSnackbar()
    }

    private fun disableInteractions() {
        binding.apply {
            resendOtp.isEnabled = false
            mobile.isEnabled = false
            mobile.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    private fun enableInteractions() {
        binding.apply {
            resendOtp.isEnabled = true
            mobile.isEnabled = true
            val editIcon = VectorDrawableCompat.create(resources, R.drawable.ic_edit, null)
            mobile.setCompoundDrawablesWithIntrinsicBounds(null, null, editIcon, null)
        }
    }

    private fun enableOtpView(enable: Boolean) {
        if (enable) {
            binding.apply {
                otp.isFocusableInTouchMode = true
                otp.isCursorVisible = true
                otp.isEnabled = true
            }
        } else {
            binding.apply {
                otp.isFocusableInTouchMode = false
                otp.isEnabled = false
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return if (blockBackButton) {
            true
        } else {
            super.onBackPressed()
        }
    }
}
