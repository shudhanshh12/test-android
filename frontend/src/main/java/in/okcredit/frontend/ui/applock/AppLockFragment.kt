package `in`.okcredit.frontend.ui.applock

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.ApplockEnableFragmentBinding
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import tech.okcredit.android.base.AppVariable
import tech.okcredit.android.base.applock.Security
import tech.okcredit.android.base.applock.Security.isKeyguardEnabled
import tech.okcredit.android.base.applock.SecurityListener
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppLockFragment :
    BaseFragment<AppLockContract.State, AppLockContract.ViewEvent, AppLockContract.Intent>(
        "AppLockScreen",
        R.layout.applock_enable_fragment
    ),
    SecurityListener {

    private val exitScreenSubject: PublishSubject<String> =
        PublishSubject.create() // true : exit app , false : exit only this screen
    private val appLockEnabledSubject: PublishSubject<String> = PublishSubject.create()
    private val appLockResumeAuthenticatedSubject: PublishSubject<String> = PublishSubject.create()
    private val turnOffLockSubject: PublishSubject<Unit> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var tracker: Tracker

    private var settingEnableLock = false

    private var putToBackground = false

    private lateinit var onViewCreatedCalledTime: DateTime
    private lateinit var onStopCalledTime: DateTime

    private val minTimeDifference = 2000 // 2 seconds

    private var isFullScreenLock = false

    private var blockBackButton = false

    @Inject
    lateinit var appvariable: AppVariable

    private val binding: ApplockEnableFragmentBinding by viewLifecycleScoped(ApplockEnableFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCalledTime = DateTime.now()
        binding.rootView.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return AppLockContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            appLockEnabledSubject
                .throttleFirst(50, TimeUnit.MILLISECONDS)
                .map { AppLockContract.Intent.AppLockEnabled(it) },

            exitScreenSubject
                .map { AppLockContract.Intent.ExitScreen(it) },

            appLockResumeAuthenticatedSubject
                .map { AppLockContract.Intent.AppLockAuthenticated(it) },

            turnOffLockSubject
                .map { AppLockContract.Intent.TurnOffLock }
        )
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_APPLOCK)
    override fun render(state: AppLockContract.State) {
    }

    private fun showLockForAppResume() {
        activity?.runOnUiThread {
            showScreen(getString(R.string.unlock_okcredit), activity as AppCompatActivity, true)
        }
    }

    private fun showLockForAppResumeDeeplink() {
        activity?.runOnUiThread {
            showScreen(getString(R.string.unlock_okcredit), activity as AppCompatActivity, false)
        }
    }

    private fun showLockForMixPanelFlow() {
        activity?.runOnUiThread {
            showScreen(getString(R.string.lock_okcredit), activity as AppCompatActivity)
        }
    }

    private fun showLockForLoginFlow() {
        activity?.runOnUiThread {
            showScreen(activity?.resources?.getString(R.string.unlock_okcredit) ?: "", activity as AppCompatActivity)
        }
    }

    private fun setupLockFromSettingScreen() {
        settingEnableLock = true
        showScreen(getString(R.string.lock_okcredit), activity as AppCompatActivity)
    }

    private fun authenticateAndTurnOffLockFromSettingScreen() {
        settingEnableLock = false
        showScreen(getString(R.string.unlock_okcredit), activity as AppCompatActivity)
    }

    override fun onBackPressed(): Boolean {
        return if (blockBackButton || isStateInitialized().not()) {
            true
        } else {
            if (getCurrentState().source == LOCK_RESUME_DEEPLINK_EXIT) {
                finishScreen(true)
                true
            } else {
                finishScreen(false)
                true
            }
        }
    }

    companion object {
        const val AUTHENTICATE_APP_RESUME_SESSION = "AUTHENTICATE_APP_RESUME_SESSION"

        const val AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK = "AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK"

        const val LOCK_SETUP_SETTING_SCREEN = "LOCK_SETUP_SETTING_SCREEN"

        const val LOCK_SETUP_INAPP_CARD = "LOCK_SETUP_INAPP_CARDL"

        const val LOCK_SETUP_LOGIN_FLOW = "LOCK_SETUP_LOGIN_FLOW"

        const val APP_EXIT = "APP_EXIT"
        const val LOCK_RESUME_EXIT = "LOCK_RESUME_EXIT"
        const val LOCK_RESUME_DEEPLINK_EXIT = "LOCK_RESUME_DEEPLINK_EXIT"
        const val APP_EXIT_VERIFICATION_SUCCESS = "APP_EXIT_VERIFICATION_SUCCESS"
        const val SCREEN_EXIT_VERIFICATION_SUCCESS = "SCREEN_EXIT_VERIFICATION_SUCCESS"
        const val ONLY_SCREEN_EXIT = "ONLY_SCREEN_EXIT"
        const val MIXPANEL_APP_SET_SCREEN_EXIT = "MIXPANEL_APP_SET_SCREEN_EXIT"

        // Lock type
        const val CUSTOM_APP_LOCK = "CUSTOM_APP_LOCK"
        const val SYSTEM_APP_LOCK = "SYSTEM_APP_LOCK"
        const val NO_APP_LOCK = "NO_APP_LOCK"

        const val ANIMATION_DURATION = 1500L

        const val LAUNCHER_FLOW = "LAUNCHER_FLOW"
        const val DEEPLINK_FLOW = "DEEPLINK_FLOW"
    }

    override fun onStop() {
        super.onStop()

        onStopCalledTime = DateTime.now()

        val difference = onStopCalledTime.millis - onViewCreatedCalledTime.millis

        isFullScreenLock = difference <= minTimeDifference

        putToBackground = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.i("onActivityResult 1")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Security.DEVICE_LOCK_SETUP_SCREEN) {
            val isDeviceSecured = activity?.let { isKeyguardEnabled(this) }

            if (isDeviceSecured != null && isDeviceSecured) {
                setupAppLock()
            } else {
                onAuthenticationFailed()
            }
        }

        if (requestCode == Security.AUTHENTICATE) {

            if (resultCode == Activity.RESULT_OK) {
                Timber.i("onActivityResult 2")
                onAuthenticationSuccess()
            } else {
                if (isFullScreenLock) {
                    onAuthenticationFailed()
                } else {
                    if (putToBackground) {
                        setupAppLock()
                    } else {
                        onAuthenticationFailed()
                    }
                }
                putToBackground = false
            }
        }
    }

    private fun setupAppLock() {
        if (isStateInitialized().not()) {
            return
        }
        if (isAdded) {
            when (getCurrentState().source) {
                AUTHENTICATE_APP_RESUME_SESSION -> showLockForAppResume()
                AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK -> showLockForAppResumeDeeplink()
                LOCK_SETUP_LOGIN_FLOW -> showLockForLoginFlow()
                LOCK_SETUP_INAPP_CARD -> showLockForMixPanelFlow()
                LOCK_SETUP_SETTING_SCREEN -> setupLockFromSettingScreen()
            }
        }
    }

    private fun onAuthenticationSuccess() {
        if (isStateInitialized().not()) {
            return
        }
        Timber.i("onActivityResult 3")
        when (getCurrentState().source) {
            AUTHENTICATE_APP_RESUME_SESSION -> {
                appLockResumeAuthenticatedSubject.onNext(LAUNCHER_FLOW)
            }
            AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK -> {
                appLockResumeAuthenticatedSubject.onNext(DEEPLINK_FLOW)
            }
            LOCK_SETUP_LOGIN_FLOW -> appLockEnabledSubject.onNext(APP_EXIT_VERIFICATION_SUCCESS)
            LOCK_SETUP_INAPP_CARD -> appLockEnabledSubject.onNext(MIXPANEL_APP_SET_SCREEN_EXIT)
            LOCK_SETUP_SETTING_SCREEN -> {
                if (settingEnableLock) {
                    appLockEnabledSubject.onNext(ONLY_SCREEN_EXIT)
                } else {
                    turnOffLockSubject.onNext(Unit)
                }
            }
        }

        appvariable.appCreated = false
    }

    private fun onAuthenticationFailed() {
        if (isStateInitialized().not()) {
            return
        }
        when (getCurrentState().source) {
            LOCK_SETUP_LOGIN_FLOW -> finishScreen(true)
            LOCK_SETUP_SETTING_SCREEN -> {
                finishScreen(false)
            }
            LOCK_SETUP_INAPP_CARD -> {
                finishScreen(false)
            }
            AUTHENTICATE_APP_RESUME_SESSION -> finishScreen(true)
            AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK -> finishScreen(true)
        }
    }

    private fun finishScreen(exitApp: Boolean) {
        if (exitApp) {
            activity?.finishAffinity()
            activity?.overridePendingTransition(0, 0)
        } else {
            activity?.finish()
            activity?.overridePendingTransition(0, 0)
        }
    }

    override fun onNoDeviceSecurity() {
        if (isStateInitialized().not()) {
            return
        }
        when (getCurrentState().source) {
            LOCK_SETUP_LOGIN_FLOW -> tracker.trackToDeviceLockSetting(screen = PropertyValue.LOGIN)
            LOCK_SETUP_SETTING_SCREEN -> tracker.trackToDeviceLockSetting(screen = PropertyValue.SETTINGS)
            LOCK_SETUP_INAPP_CARD -> tracker.trackToDeviceLockSetting(screen = PropertyValue.HOME_PAGE)
            AUTHENTICATE_APP_RESUME_SESSION -> tracker.trackToDeviceLockSetting(screen = PropertyValue.RELAUNCH)
            AUTHENTICATE_APP_RESUME_SESSION_DEEPLINK -> tracker.trackToDeviceLockSetting(screen = PropertyValue.RELAUNCH)
        }
    }

    // In case if any error, we user to HomeScreen without authenticating
    override fun onError(appResume: Boolean) {
        if (appResume) {
            context?.let { legacyNavigator.get().goToHome(requireActivity()) }
            finishScreen(true)
        } else {
            blockBackButton = true
            showSuccessAnimation {
                legacyNavigator.get().goToHome(requireActivity())
                finishScreen(true)
            }
        }
    }

    private fun showScreen(title: String, activity: AppCompatActivity, appResume: Boolean = false) {
        if (isAdded) {
            Security.authenticate(title, activity, this, appResume)
        } else {
            onError(appResume)
        }
    }

    private fun showSuccessAnimation(gotoHome: () -> Unit) {
        blockBackButton = true
        binding.apply {
            groupSuccess.visible()
            lottieOtpVerifySuccess.playAnimation()
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(dispatcherProvider.get().main()) {
                    delay(ANIMATION_DURATION)
                    gotoHome()
                }
            }
        }
    }

    override fun handleViewEvent(event: AppLockContract.ViewEvent) {
        when (event) {
            AppLockContract.ViewEvent.GoToLogin -> {
                legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
            }

            AppLockContract.ViewEvent.AuthenticateAppResume -> {
                showLockForAppResume()
            }

            AppLockContract.ViewEvent.AuthenticateAppResumeDeeplink -> {
                showLockForAppResumeDeeplink()
            }

            AppLockContract.ViewEvent.SetupLockFromSettingScreen -> {
                setupLockFromSettingScreen()
            }

            AppLockContract.ViewEvent.AuthenticateAndTurnOffLockFromSettingScreen -> {
                authenticateAndTurnOffLockFromSettingScreen()
            }

            AppLockContract.ViewEvent.SetupLockMixpanelInAppNotiFlow -> {
                showLockForMixPanelFlow()
            }

            AppLockContract.ViewEvent.SetupLockForExistingUserLoginFlow -> {
                showLockForLoginFlow()
            }

            is AppLockContract.ViewEvent.EXIT -> {
                Timber.i("onActivityResult exit=${event.exitType}")
                when (event.exitType) {
                    LOCK_RESUME_EXIT -> {
                        context?.let { legacyNavigator.get().goToHome(requireActivity()) }
                        finishScreen(true)
                    }

                    LOCK_RESUME_DEEPLINK_EXIT -> {
                        finishScreen(false)
                    }

                    APP_EXIT -> {
                        finishScreen(true)
                    }

                    ONLY_SCREEN_EXIT -> {
                        Timber.i("onActivityResult 5")
                        finishScreen(false)
                    }
                    APP_EXIT_VERIFICATION_SUCCESS -> {
                        showSuccessAnimation {
                            legacyNavigator.get().goToSyncScreen(requireActivity())
                            finishScreen(true)
                        }
                    }

                    SCREEN_EXIT_VERIFICATION_SUCCESS -> {
                        showSuccessAnimation {
                            finishScreen(false)
                        }
                    }

                    MIXPANEL_APP_SET_SCREEN_EXIT -> {
                        binding.tvAuthenticationSuccess.text = getString(R.string.app_lock_enabled)
                        showSuccessAnimation {
                            finishScreen(false)
                        }
                    }
                }
            }
        }
    }
}
