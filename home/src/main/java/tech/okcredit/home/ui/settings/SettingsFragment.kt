package tech.okcredit.home.ui.settings

import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.OnSetPinClickListener
import tech.okcredit.contract.OnUpdatePinClickListener
import tech.okcredit.contract.SET_PIN
import tech.okcredit.contract.UPDATE_PIN
import tech.okcredit.home.R
import tech.okcredit.home.databinding.SettingsFragmentBinding
import tech.okcredit.home.ui.settings.SettingsActivity.Companion.LAUNCH_APP_LOCK
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.CHANGE_NUMBER_CLICKED
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.FINGERPRINT_LOCK
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.FINGERPRINT_UNLOCK
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.FINGERPRINT_UNLOCK_DISABLED
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.FINGERPRINT_UNLOCK_ENABLED
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.LIST
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.MERCHANT
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.PAYMENT_DISABLE
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.PAYMENT_ENABLE
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.REQUEST_PASSWORD_SETTING
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.SECURITY_LOGOUT_CLICKED
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.SECURITY_LOGOUT_FROM_ALL_DEVICES_CLICKED
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.SETTINGS_PAGE
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.SIGNOUT
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.TRUE
import tech.okcredit.home.ui.settings.analytics.SettingsEventTracker
import tech.okcredit.home.ui.settings.dialogs.AppPinSetScreen
import tech.okcredit.home.ui.settings.dialogs.ConfirmSignoutScreen
import tech.okcredit.home.utils.CUSTOM_APP_LOCK
import tech.okcredit.home.utils.LOCK_SETUP_SETTING_SCREEN
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsFragment :
    BaseFragment<SettingsContract.State, SettingsContract.ViewEvent, SettingsContract.Intent>(
        "SettingsScreen",
        contentLayoutId = R.layout.settings_fragment
    ),
    ConfirmSignoutScreen.OnSignoutListener,
    OnSetPinClickListener,
    OnUpdatePinClickListener {

    private var alert: Snackbar? = null
    internal val binding: SettingsFragmentBinding by viewLifecycleScoped(SettingsFragmentBinding::bind)

    @Inject
    internal lateinit var appLock: Lazy<AppLock>

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var settingsEventTracker: Lazy<SettingsEventTracker>

    @Inject
    internal lateinit var paymentNavigator: Lazy<PaymentNavigator>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey50
                )
            )
        )
        initListeners()
        binding.rootView.setTracker(performanceTracker)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        settingsEventTracker.get().trackSecurityScreenEvents(SETTINGS_PAGE)
    }

    private fun initListeners() {
        binding.tvProfile.setOnClickListener { pushIntent(SettingsContract.Intent.ProfileClick) }
        binding.tvLanguage.setOnClickListener { pushIntent(SettingsContract.Intent.AppLanguageClick) }
        binding.tvUpdatePassword.setOnClickListener {
            settingsEventTracker.get().trackSecurityScreenEvents(REQUEST_PASSWORD_SETTING)
            pushIntent(SettingsContract.Intent.UpdatePasswordClick(false))
        }
        binding.tvEnableFingerprint.setOnClickListener {

            if (getCurrentState().isFingerPrintLockVisible && getCurrentState().isFingerPrintEnabled) {
                settingsEventTracker.get()
                    .trackSecurityScreenEvents(eventName = FINGERPRINT_UNLOCK, flow = SETTINGS_PAGE)
            } else if (getCurrentState().isFingerPrintLockVisible && !getCurrentState().isFingerPrintEnabled) {
                settingsEventTracker.get().trackSecurityScreenEvents(eventName = FINGERPRINT_LOCK, flow = SETTINGS_PAGE)
            }

            if (getCurrentState().isSetPassword) {
                if (getCurrentState().isMerchantPrefSync) {
                    if (getCurrentState().isFourDigitPinSet) {
                        startActivityForResult(
                            appLock.get().appLock(
                                getString(R.string.enterpin_screen_deeplink), requireActivity(),
                                SETTINGS_PAGE
                            ),
                            FINGERPRINT_ENABLE
                        )
                    } else {
                        pushIntent(SettingsContract.Intent.CheckIsFourDigit(SettingsClicks.FINGERPRINT_CLICK))
                    }
                } else {
                    pushIntent(SettingsContract.Intent.SyncMerchantPref(SettingsClicks.FINGERPRINT_CLICK))
                }
            } else {
                shortToast(getString(R.string.set_security_pin_msg))
            }
        }

        binding.tvAppLock.setOnClickListener { pushIntent(SettingsContract.Intent.AppLockClick) }
        binding.tvPaymentPassword.setOnClickListener {
            if (getCurrentState().isPaymentPasswordEnabled) {
                settingsEventTracker.get().trackSecurityScreenEvents(eventName = PAYMENT_DISABLE, flow = SETTINGS_PAGE)
            } else {
                settingsEventTracker.get().trackSecurityScreenEvents(eventName = PAYMENT_ENABLE, flow = SETTINGS_PAGE)
            }
            pushIntent(SettingsContract.Intent.PaymentPasswordClick)
        }
        binding.tvSignoutAll.setOnClickListener {
            settingsEventTracker.get().trackSecurityScreenEvents(SECURITY_LOGOUT_FROM_ALL_DEVICES_CLICKED)
            callIntent()
        }
        binding.tvSignout.setOnClickListener { signOut() }
        binding.tvChangeNumber.setOnClickListener {
            settingsEventTracker.get().trackSecurityScreenEvents(CHANGE_NUMBER_CLICKED, SETTINGS_PAGE)
            pushIntent(SettingsContract.Intent.ChangeNumberClick)
        }
        binding.tvUpiOptions.setOnClickListener {
            paymentNavigator.get().startPspUpiActivity(requireActivity())
        }
    }

    private fun callIntent() {
        if (isStateInitialized().not()) return

        if (getCurrentState().isSetPassword) {
            if (getCurrentState().isMerchantPrefSync) {
                if (getCurrentState().isFourDigitPinSet) {
                    pushIntent(SettingsContract.Intent.SignOutFromAllDevices)
                } else {
                    pushIntent(SettingsContract.Intent.CheckIsFourDigit((SettingsClicks.SIGNOUT_CLICK)))
                }
            } else {
                pushIntent(SettingsContract.Intent.SyncMerchantPref(SettingsClicks.SIGNOUT_CLICK))
            }
        } else {
            pushIntent(SettingsContract.Intent.SetNewPin(SettingsClicks.SIGNOUT_CLICK))
        }
    }

    private fun signOut() {
        settingsEventTracker.get().trackSecurityScreenEvents(SECURITY_LOGOUT_CLICKED)
        showConfirmSignoutScreen()
    }

    override fun loadIntent(): UserIntent {
        return SettingsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(SettingsContract.Intent.Resume)
        )
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: SettingsContract.State) {

        if (state.signOut) {
            legacyNavigator.goToLoginScreen(requireActivity())
            requireActivity().finishAffinity()
        }

        // show/hide alert
        if (state.networkError or state.error or state.isAlertVisible) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }

        if (state.activeLangugeStringId != -1) binding.activeLanguage.text =
            requireContext().getString(state.activeLangugeStringId)

        binding.switchAppLock.isChecked = state.isAppLockActive
        binding.switchPaymentPassword.isChecked = state.isPaymentPasswordEnabled

        if (state.isSetPassword) {
            binding.tvUpdatePassword.text = getString(R.string.change_security_pin)
            binding.groupSecurityPinOptions.visible()
        } else {
            binding.tvUpdatePassword.text = getString(R.string.set_security_pin)
            binding.groupSecurityPinOptions.gone()
        }

        binding.swFingerPrint.isChecked = state.isFingerPrintEnabled

        renderFingerPrint(state)
        setUpiBlockVisibility(state)
    }

    private fun setUpiBlockVisibility(state: SettingsContract.State) {
        if (state.isPspUpiFeatureEnabled) {
            binding.apply {
                vwUpiOptions.visible()
                tvUpiOptions.visible()
            }
        } else {
            binding.apply {
                vwUpiOptions.gone()
                tvUpiOptions.gone()
            }
        }
    }

    private fun renderFingerPrint(state: SettingsContract.State) {
        val showFingerprint = state.isSetPassword && state.isFingerPrintLockVisible
        binding.tvEnableFingerprint.isVisible = showFingerprint
        binding.swFingerPrint.isVisible = showFingerprint
    }

    private fun goToAppLockScreen() {
        if (getCurrentState().appLockType == CUSTOM_APP_LOCK) {
            legacyNavigator.goToAppLockScreen(requireActivity())
        } else {
            legacyNavigator.goToSystemAppLockScreen(requireActivity(), LOCK_SETUP_SETTING_SCREEN)
        }
    }

    private fun openLogoutConfirmationDialog() {
        requireActivity().runOnUiThread {
            startActivityForResult(
                appLock.get().appLock(getString(R.string.enterpin_screen_deeplink), requireActivity(), SETTINGS_PAGE),
                SIGNOUT_FROM_ALLDEVICES
            )
        }
    }

    fun gotoLogin() {
        legacyNavigator.goToLoginScreen(requireActivity())
        activity?.finishAffinity()
    }

    override fun handleViewEvent(event: SettingsContract.ViewEvent) {
        when (event) {
            is SettingsContract.ViewEvent.GoToProfileScreen -> {
                settingsEventTracker.get().trackViewProfile(SETTINGS_PAGE, MERCHANT, null)
                legacyNavigator.goToMerchantProfile(requireActivity())
            }
            is SettingsContract.ViewEvent.GoToLanguageScreen -> {
                settingsEventTracker.get().trackViewLanguage(SETTINGS_PAGE, TRUE, LIST)
                legacyNavigator.goToLanguageScreen(requireActivity())
            }
            is SettingsContract.ViewEvent.GotoResetPasswordScreen -> {
                settingsEventTracker.get().trackPasswordChangeClickesEvents()
                requireActivity().startActivityForResult(
                    appLock.get().appLock(
                        getString(R.string.changepin_screen_deeplink),
                        requireActivity(),
                        SETTINGS_PAGE,
                        if (getCurrentState().isSetPassword) UPDATE_PIN else SET_PIN
                    ),
                    LAUNCH_APP_LOCK
                )
            }
            is SettingsContract.ViewEvent.OpenAppLock -> goToAppLockScreen()
            is SettingsContract.ViewEvent.GoToPaymentPasswordEnableScreen -> {
                legacyNavigator.goToPasswordEnableScreen(
                    requireActivity()
                )
            }
            is SettingsContract.ViewEvent.OpenLogoutConfirmationDialog -> openLogoutConfirmationDialog()

            is SettingsContract.ViewEvent.gotoLogin -> gotoLogin()
            is SettingsContract.ViewEvent.GoToChangeNumberScreen -> {
                startActivity(legacyNavigator.getNumberChangeScreenIntent())
            }
            is SettingsContract.ViewEvent.ShowInvalidPassword -> view?.snackbar(
                getString(R.string.account_incorrect_password),
                Snackbar.LENGTH_SHORT
            )?.show()
            is SettingsContract.ViewEvent.GoToSetNewPinScreen -> {
                when (event.type) {
                    is SettingsClicks.SIGNOUT_CLICK -> showNewPinSetDialog(SIGNOUT_FROM_ALLDEVICES)
                    is SettingsClicks.FINGERPRINT_CLICK -> showNewPinSetDialog(FINGERPRINT_ENABLE)
                }
            }
            is SettingsContract.ViewEvent.ShowUpdatePinDialog -> {
                when (event.type) {
                    is SettingsClicks.SIGNOUT_CLICK -> appLock.get().showUpdatePin(
                        requireActivity().supportFragmentManager, this,
                        SIGNOUT_FROM_ALLDEVICES,
                        SETTINGS_PAGE
                    )
                    is SettingsClicks.FINGERPRINT_CLICK -> appLock.get().showUpdatePin(
                        requireActivity().supportFragmentManager, this,
                        FINGERPRINT_ENABLE,
                        SETTINGS_PAGE
                    )
                }
            }

            is SettingsContract.ViewEvent.SyncDone -> {
                pushIntent(SettingsContract.Intent.CheckIsFourDigit(event.type))
            }

            is SettingsContract.ViewEvent.CheckFourDigitPinDone -> {
                when (event.type) {
                    is SettingsClicks.FINGERPRINT_CLICK -> {
                        if (event.isFourDigitPinSet) {
                            startActivityForResult(
                                appLock.get().appLock(
                                    getString(R.string.enterpin_screen_deeplink),
                                    requireActivity(),
                                    SETTINGS_PAGE
                                ),
                                FINGERPRINT_ENABLE
                            )
                        } else
                            pushIntent(SettingsContract.Intent.UpdatePin(event.type))
                    }

                    is SettingsClicks.SIGNOUT_CLICK -> {
                        if (event.isFourDigitPinSet)
                            pushIntent(SettingsContract.Intent.SignOutFromAllDevices)
                        else
                            pushIntent(SettingsContract.Intent.UpdatePin(event.type))
                    }
                }
            }
        }
    }

    private fun showNewPinSetDialog(requestCode: Int) {
        activity?.runOnUiThread {
            appLock.get().showSetNewPin(requireActivity().supportFragmentManager, this, requestCode, SETTINGS_PAGE)
        }
    }

    private fun showConfirmSignoutScreen() {
        activity?.runOnUiThread {
            val bottomSheet = ConfirmSignoutScreen.newInstance()
            bottomSheet.initialise(this)
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        }
    }

    override fun onSignoutClicked() {
        settingsEventTracker.get().trackSecurityScreenEvents(SIGNOUT)
        pushIntent(SettingsContract.Intent.SignOut)
    }

    companion object {
        const val SIGNOUT_FROM_ALLDEVICES = 171
        const val FINGERPRINT_ENABLE = 10001
        const val IS_AUTHENTICATED = "IS_AUTHENTICATED"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGNOUT_FROM_ALLDEVICES) {
            data?.let {
                if (it.getBooleanExtra(IS_AUTHENTICATED, false)) {
                    Completable
                        .timer(100, TimeUnit.MILLISECONDS)
                        .observeOn(schedulerProvider.get().ui())
                        .subscribe {
                            pushIntent(SettingsContract.Intent.SignoutConfirmationClick)
                        }.addTo(autoDisposable)
                }
            }
        }
        if (requestCode == FINGERPRINT_ENABLE) {
            if (data != null) {
                val isAuthenticated = data.getBooleanExtra(IS_AUTHENTICATED, false)
                if (isAuthenticated) {
                    setFingerPrintEnableUI()
                }
            }
        }
    }

    private fun setFingerPrintEnableUI() {
        if (isStateInitialized().not()) return

        val state = getCurrentState()
        binding.swFingerPrint.isChecked = !state.isFingerPrintEnabled

        val fingerprintStatus =
            if (binding.swFingerPrint.isChecked) FINGERPRINT_UNLOCK_ENABLED else FINGERPRINT_UNLOCK_DISABLED
        settingsEventTracker.get().trackSecurityScreenEvents(fingerprintStatus, SETTINGS_PAGE)

        pushIntentWithDelay(SettingsContract.Intent.SetFingerPrintEnable(!state.isFingerPrintEnabled))
        if (!state.isFingerPrintEnabled) {
            val bottomSheet = AppPinSetScreen.newInstance(getString(R.string.app_fingerprint_lock_enabled))
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
        }
    }

    override fun onSetPinClicked(requestCode: Int) {
        requireActivity().runOnUiThread {
            startActivityForResult(
                appLock.get().appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), SETTINGS_PAGE),
                requestCode
            )
        }
    }

    override fun onDismissed() {
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        requireActivity().runOnUiThread {
            startActivityForResult(
                appLock.get().appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), SETTINGS_PAGE),
                requestCode
            )
        }
    }

    override fun onUpdateDialogDismissed() {
    }
}
