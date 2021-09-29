package tech.okcredit.applock.pinLock

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.os.Bundle
import android.view.View
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.applock.R
import tech.okcredit.applock.analytics.AppLockEventProperties.PIN_ENTRY_STARTED
import tech.okcredit.applock.analytics.AppLockEventProperties.PIN_RE_ENTERED_MATCH
import tech.okcredit.applock.analytics.AppLockEventProperties.PIN_RE_ENTERED_MIS_MATCH
import tech.okcredit.applock.analytics.AppLockEventTracker
import tech.okcredit.applock.databinding.SetPinFragmentBinding
import tech.okcredit.applock.pinLock.PinLockContract.*
import tech.okcredit.contract.Constants.IS_AUTHENTICATED
import javax.inject.Inject

class PinLockFragment : BaseFragment<State, ViewEvent, Intent>(
    "PinLockScreen", R.layout.set_pin_fragment
) {
    private val binding: SetPinFragmentBinding by viewLifecycleScoped(SetPinFragmentBinding::bind)
    private var enableConfirmPin: Boolean = false

    @Inject
    lateinit var appLockEventTracker: Lazy<AppLockEventTracker>

    companion object {
        const val NEW_PIN = "New Pin"
        const val UPDATE_PIN = "Pin Updated"
        const val PIN_STATUS = "PinStatus"
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.disableScreanCapture()
        super.onViewCreated(view, savedInstanceState)
        binding.pinView.setPinViewEventListener { pinview, b ->
            appLockEventTracker.get()
                .trackEvents(
                    eventName = PIN_ENTRY_STARTED, flow = getCurrentState().source,
                    entry = getCurrentState().entry
                )
            pushIntent(Intent.SetPin(pinview.value))
        }
        binding.confirmPin.setPinViewEventListener { pinview, b ->
            pushIntent(Intent.ConfirmPin(pinValue = getCurrentState().pinValue, confirmPin = pinview.value))
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.rootView.setTracker(performanceTracker)
    }

    override fun onResume() {
        super.onResume()
        binding.confirmPin.post {
            if (enableConfirmPin) {
                binding.confirmPin.requestFocus()
                binding.pinView.isFocusable = false
            } else {
                binding.pinView.isFocusable = true
                binding.pinView.requestFocus()
            }
        }
    }

    override fun render(state: State) {
        setValidationStatus(state)
    }

    private fun setValidationStatus(state: State) {
        if (state.isIncorrectPin) {
            appLockEventTracker.get()
                .trackEvents(
                    eventName = PIN_RE_ENTERED_MIS_MATCH, flow = getCurrentState().source,
                    entry = getCurrentState().entry
                )
            binding.confirmPin.clearOtp()
            binding.tvInvalidPin.visible()
        } else
            binding.tvInvalidPin.gone()
    }

    fun showConfirmPin() {
        binding.tvSetpin.text = getString(R.string.confirm_pin)
        binding.pinView.gone()
        binding.confirmPin.visible()
        enableConfirmPin = true
        binding.confirmPin.requestFocus()
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.AskConfirmPin -> showConfirmPin()
            ViewEvent.UpdateMerchantPref -> {
                pushIntent(Intent.FourDigitPinUpdated)
            }
            ViewEvent.PinVerified -> {
                appLockEventTracker.get()
                    .trackEvents(
                        eventName = PIN_RE_ENTERED_MATCH, flow = getCurrentState().source,
                        entry = getCurrentState().entry
                    )
                hideSoftKeyboard()
                val returnIntent = requireActivity().intent
                if (getCurrentState().isUpdatePin) {
                    returnIntent.putExtra(PIN_STATUS, UPDATE_PIN)
                } else {
                    returnIntent.putExtra(PIN_STATUS, NEW_PIN)
                }
                returnIntent.putExtra(IS_AUTHENTICATED, true)
                requireActivity().setResult(Activity.RESULT_OK, returnIntent)
                requireActivity().finish()
            }
            ViewEvent.ShowIncorrectPin -> {
                appLockEventTracker.get()
                    .trackEvents(
                        eventName = PIN_RE_ENTERED_MIS_MATCH, flow = getCurrentState().source,
                        entry = getCurrentState().entry
                    )
                binding.confirmPin.clearOtp()
                binding.tvInvalidPin.visible()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        hideSoftKeyboard()
        requireActivity().finish()
        return false
    }

    override fun loadIntent(): UserIntent? {
        return Intent.Load
    }
}
