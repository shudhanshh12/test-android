package tech.okcredit.applock.enterPin

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.applock.AppLockActivityV2.Companion.ENTRY
import tech.okcredit.applock.R
import tech.okcredit.applock.analytics.AppLockEventProperties
import tech.okcredit.applock.analytics.AppLockEventProperties.SECURITY_PIN_MATCH
import tech.okcredit.applock.analytics.AppLockEventProperties.SECURITY_PIN_MIS_MATCH
import tech.okcredit.applock.analytics.AppLockEventTracker
import tech.okcredit.applock.databinding.EnterPinFragmentBinding
import tech.okcredit.contract.Constants.IS_AUTHENTICATED
import tech.okcredit.contract.Constants.PIN
import tech.okcredit.contract.FORGOT_PIN
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EnterPinFragment :
    BaseFragment<EnterPinContract.State, EnterPinContract.ViewEvent, EnterPinContract.Intent>(
        "EnterPinScreen", R.layout.enter_pin_fragment
    ) {
    internal val binding: EnterPinFragmentBinding by viewLifecycleScoped(EnterPinFragmentBinding::bind)

    @Inject
    lateinit var appLockEventTracker: Lazy<AppLockEventTracker>

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator
    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().window.disableScreanCapture()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun render(state: EnterPinContract.State) {
        renderInvalidPIN(state)
        renderUseFingerPrint(state)
    }

    private fun renderUseFingerPrint(state: EnterPinContract.State) {
        binding.useFingerPrintBtn.isVisible = state.isFingerPrintEnabled && state.isFingerprintEnrolledInDevice
    }

    private fun renderInvalidPIN(state: EnterPinContract.State) {
        if (state.incorrectPin) {
            if (binding.pinView.value.isNotEmpty())
                binding.pinView.clearOtp()
            appLockEventTracker.get().trackEvents(eventName = SECURITY_PIN_MIS_MATCH, flow = state.source)
        }
        binding.tvInvalidPin.isVisible = state.incorrectPin
    }

    override fun handleViewEvent(event: EnterPinContract.ViewEvent) {
        when (event) {
            is EnterPinContract.ViewEvent.GotoHomeScreen -> {
                requireActivity().runOnUiThread {
                    legacyNavigator.goToHome(requireActivity())
                    requireActivity().finishAffinity()
                }
            }
            is EnterPinContract.ViewEvent.AuthError -> {
                requireActivity().runOnUiThread {
                    legacyNavigator.goToLoginScreen(requireActivity())
                }
            }
            is EnterPinContract.ViewEvent.InternetError -> {
                shortToast(getString(R.string.no_internet_connection))
            }
            is EnterPinContract.ViewEvent.ShowMessageWithRetry -> {
                view?.snackbar(getString(R.string.unable_to_delete_customer_balance_not_zero), Snackbar.LENGTH_SHORT)
                    ?.show()
            }
            is EnterPinContract.ViewEvent.Authenticated -> {
                appLockEventTracker.get()
                    .trackEvents(eventName = SECURITY_PIN_MATCH, flow = getCurrentState().source)
                returnResult(event.pin)
            }
            is EnterPinContract.ViewEvent.GoToSetPinScreen -> {
                activity?.intent?.apply {
                    putExtra(ENTRY, FORGOT_PIN)
                }
                findNavController().navigate(Uri.parse(getString(R.string.changepin_screen_deeplink)))
            }
            is EnterPinContract.ViewEvent.ShowInputMode -> {
                if (event.isShowFingerprint) {
                    hideSoftKeyboard()
                    showBiometric()
                } else {
                    binding.pinView.setForceKeyboard(true)
                    binding.pinView.performClick()
                }
            }
        }
    }

    fun returnResult(pin: String? = null) {
        hideSoftKeyboard()
        requireActivity().runOnUiThread {
            val returnIntent = Intent()
            returnIntent.putExtra(PIN, pin)
            returnIntent.putExtra(IS_AUTHENTICATED, true)
            activity?.setResult(RESULT_OK, returnIntent)
            activity?.finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pinView.setPinViewEventListener { pinview, b ->
            pushIntent(EnterPinContract.Intent.VerifyPin(pinview.value))
        }

        binding.useFingerPrintBtn.setOnClickListener {
            hideSoftKeyboard()
            showBiometric()
        }

        binding.forgotPin.setOnClickListener {
            if (binding.pinView.value.isNotEmpty())
                binding.pinView.clearOtp()
            binding.tvInvalidPin.gone()
            appLockEventTracker.get()
                .trackEvents(eventName = AppLockEventProperties.FORGOT_PASSWORD_CLICK, flow = getCurrentState().source)
            pushIntent(EnterPinContract.Intent.ForgotPin)
        }
        binding.rootView.setTracker(performanceTracker)
    }

    companion object {
        fun newInstance(): EnterPinFragment {
            return EnterPinFragment()
        }
    }

    override fun onBackPressed(): Boolean {
        hideSoftKeyboard()
        requireActivity().finish()
        return true
    }

    private fun showBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    returnResult()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Completable
                            .timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(schedulerProvider.get().ui())
                            .subscribe {
                                binding.pinView.setForceKeyboard(true)
                                binding.pinView.performClick()
                            }.addTo(autoDisposable)
                    }
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.confirm_fingerprint))
            .setSubtitle(getString(R.string.confirm_fingerprint_description))
            .setNegativeButtonText(getString(R.string.use_pin))
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    override fun loadIntent(): UserIntent? {
        return EnterPinContract.Intent.Load
    }
}
