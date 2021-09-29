package `in`.okcredit.onboarding.enterotp

import `in`.aabhasjindal.otptextview.OTPListener
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.onboarding.databinding.EnterOtpFragmentBinding
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.SmsHelper
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.*
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EnterOtpFragment :
    BaseFragment<EnterOtpContract.State, EnterOtpContract.ViewEvent, EnterOtpContract.Intent>(
        "EnterOtpScreen",
        R.layout.enter_otp_fragment
    ) {

    private fun goBackWithError(error: String) {
        requireActivity().setResult(RESULT_OK, Intent().putExtra("error", error))
        requireActivity().finish()
    }

    private var alert: Snackbar? = null

    private val sendOtpPublishSubject: PublishSubject<Pair<Boolean, String>> = PublishSubject.create()
    private val resendOtpPublishSubject: PublishSubject<Unit> = PublishSubject.create()
    private val otpReadFailedSubject: PublishSubject<String> = PublishSubject.create()
    private val gotoEnterMobileSubject: PublishSubject<Unit> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var smsHelper: Lazy<SmsHelper>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    @Inject
    lateinit var deviceUtils: Lazy<DeviceUtils>

    @Inject
    lateinit var localeManager: Lazy<LocaleManager>

    private val tasks = CompositeDisposable()

    internal val binding: EnterOtpFragmentBinding by viewLifecycleScoped(EnterOtpFragmentBinding::bind)

    private var otpCountDownTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.disableScreanCapture()

        binding.otp.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                if (binding.otp.otp?.length != 6) {
                    binding.tvInCorrectPin.visibility = View.INVISIBLE
                }
            }

            override fun onOTPComplete(otp: String) {
                sendOtpPublishSubject.onNext(false to otp)
            }
        }

        binding.textViewWrongNumber.setOnClickListener {
            gotoEnterMobileSubject.onNext(Unit)
        }

        binding.scrollView.setTracker(performanceTracker)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        KeyboardVisibilityEvent.stateHiddentWindowSoftInputMode(activity)
    }

    override fun loadIntent(): UserIntent {
        return EnterOtpContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            sendOtpPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Timber.i("VerifyOTP ${it.first}")
                    Timber.d("<<< sendOtpPublishSubject ${it.first}")
                    EnterOtpContract.Intent.VerifyOtp(it.first, it.second)
                },

            resendOtpPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    EnterOtpContract.Intent.ResendOtp
                },

            otpReadFailedSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    EnterOtpContract.Intent.OtpReadFailed(it)
                },

            gotoEnterMobileSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    EnterOtpContract.Intent.EnterMobile
                },

            binding.textViewResendSmsOption
                .clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { EnterOtpContract.Intent.SendOtpViaSms },

            binding.textViewResendWhatsappOption
                .clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { EnterOtpContract.Intent.SendOtpViaWhatsApp },

            binding.textViewResendCallOption
                .clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { EnterOtpContract.Intent.SendOtpViaIvr }
        )
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_ENTER_OTP)
    override fun render(state: EnterOtpContract.State) {

        try {
            KeyboardVisibilityEvent.setEventListener(requireContext() as Activity) { isOpen ->
                if (isOpen) {
                    binding.scrollView.smoothScrollTo(0, binding.scrollView.bottom)
                } else {
                    binding.scrollView.smoothScrollTo(0, binding.scrollView.top)
                }
            }
        } catch (e: java.lang.Exception) {
        }

        if (state.otpError && binding.otp.otp?.length == 6) {
            binding.tvInCorrectPin.visible()
        } else {
            binding.tvInCorrectPin.invisible()
        }

        binding.tvInCorrectPin.visibility =
            if (state.otpError && binding.otp.otp?.length == 6) View.VISIBLE else View.INVISIBLE

        binding.title.text = getString(R.string.t_001_login_title_enter_otp, state.mobile)

        val wrongNumberText = SpannableString(getString(R.string.t_001_login_cta_wrong_number))
        wrongNumberText.setSpan(UnderlineSpan(), 0, wrongNumberText.length, 0)
        binding.textViewWrongNumber.text = wrongNumberText

        // show/hide alert
        if (state.networkError or state.networkErrorWithRetry or state.error or state.isAlertVisible) {
            val text = getString(R.string.home_no_internet_msg)
            alert = when {
                state.networkErrorWithRetry ->
                    Snackbar
                        .make(
                            requireView(),
                            Html.fromHtml("<font color=\"#ffffff\">$text</font>"),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        .setAction(getString(R.string.retry)) {
                            resendOtpPublishSubject.onNext(Unit)
                        }
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

        if (state.logout) {
            legacyNavigator.get().gotoWelcomeLanguageSelectionScreen(requireContext())
            activity?.finishAffinity()
        }
        if (state.canShowMigrationSuccessfulView) {
            if (activity != null) {
                KeyboardVisibilityEvent.hideKeyboard(activity)
            }
            binding.otpView.gone()
            binding.migrationSuccessfulView.visible()
        } else {
            binding.otpView.visible()
            binding.migrationSuccessfulView.gone()
        }
        if (state.merchantAlreadyExistsError) {

            view?.snackbar(getString(R.string.merchant_already_exists), Snackbar.LENGTH_INDEFINITE)?.show()
        }

        val showEditMobileOption =
            (
                state.flag != OnboardingConstants.FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER &&
                    state.flag != OnboardingConstants.FLAG_NUMBER_CHANGE
                ) && state.verifiedSuccessfully.not()

        if (showEditMobileOption) {
            binding.textViewWrongNumber.visible()
        } else {
            binding.textViewWrongNumber.gone()
        }

        if (state.isLoading) {
            binding.imageViewVerificationSuccessful.gone()
            binding.progressBar.visible()
        } else {
            binding.progressBar.gone()
        }

        handleFallbackOptions(state)
    }

    private fun handleFallbackOptions(state: EnterOtpContract.State) {
        if (!state.showFallbackOption || state.fallbackOptions.isEmpty()) {
            resetFallbackOptions()
            return
        }

        if (binding.textViewTimer.visibility == View.INVISIBLE) {
            return
        }

        binding.textViewTimer.invisible()

        state.fallbackOptions.forEach { medium ->
            val fallbackView = when (RequestOtpMedium.getMedium(medium)) {
                RequestOtpMedium.SMS -> binding.textViewResendSmsOption
                RequestOtpMedium.WHATSAPP -> binding.textViewResendWhatsappOption
                RequestOtpMedium.CALL -> binding.textViewResendCallOption
            }
            fallbackView.visible()
            AnimationUtils.fadeIn(fallbackView)
        }
    }

    private fun resetFallbackOptions() {
        binding.textViewResendSmsOption.gone()
        binding.textViewResendWhatsappOption.gone()
        binding.textViewResendCallOption.gone()
    }

    override fun onResume() {
        super.onResume()
        binding.otp.requestFocusOTP()
        hideSoftKeyboard()
        tasks.add(
            smsHelper.get()
                .otp()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { otpValue ->
                        Timber.d("<<< Got Otp")
                        Timber.i("VerifyOTP $otpValue")
                        sendOtpPublishSubject.onNext(true to otpValue)
                        binding.otp.setOTP(otpValue)
                    },
                    { throwable ->
                        otpReadFailedSubject.onNext(throwable.message ?: "")
                        Timber.e(throwable, "otp auto read channel failed")
                    }
                )
        )

        smsHelper.get().startListening()
    }

    override fun onPause() {
        super.onPause()
        smsHelper.get().stopListening()
    }

    private fun gotoLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun gotToEnterNewNumberScreen() {
        activity?.let { legacyNavigator.get().goToChangeNumberScreen(it) }
        activity?.finish()
    }

    private fun goToHome() {
        showVerificationSuccessful()
        activity?.let { legacyNavigator.get().goToHome(it) }
        activity?.finishAffinity()
    }

    private fun showVerificationSuccessful() {
        binding.progressBar.gone()
        binding.imageViewVerificationSuccessful.visible()
    }

    private fun goToSyncDataScreen() {
        hideSoftKeyboard()
        binding.progressBar.visible()
        tasks.add(
            Completable.timer(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showVerificationSuccessful()
                    activity?.let {
                        legacyNavigator.get().goToSyncScreen(it)
                        it.finishAffinity()
                    }
                }
        )
    }

    private fun goToEnterNameScreen() {
        hideSoftKeyboard()
        binding.progressBar.visible()
        tasks.add(
            Completable.timer(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showVerificationSuccessful()
                    activity?.let {
                        onboardingAnalytics.get().trackNameScreen(OnboardingAnalytics.OnboardingEvent.ENTER_NAME_SCREEN)
                        legacyNavigator.get().goToOnboardBusinessNameScreen(it)
                        it.finishAffinity()
                    }
                }
        )
    }

    private fun goToAppLockAuthentication() {
        context?.let { context ->
            showVerificationSuccessful()
            legacyNavigator.get().goToSystemAppLockScreenFromLogin(context)
            activity?.finishAffinity()
        }
    }

    private fun gotoEnterMobileScreen() {
        context?.let {
            legacyNavigator.get().goToEnterMobileScreen(context = it, mobileNumber = getCurrentState().mobile)
        }
        activity?.finish()
    }

    override fun onBackPressed(): Boolean {
        val verified = isStateInitialized() && getCurrentState().verifiedSuccessfully
        val normalBackFlow =
            isStateInitialized() && (
                getCurrentState().googlePopUp ||
                    getCurrentState().flag == OnboardingConstants.FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER ||
                    getCurrentState().flag == OnboardingConstants.FLAG_NUMBER_CHANGE
                )

        // when verify successful animation is playing , we don't allow user to press back button
        return if (verified.not()) {
            if (normalBackFlow) {
                super.onBackPressed()
            } else {
                gotoEnterMobileScreen()
                true
            }
        } else {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        otpCountDownTimer?.cancel()
        tasks.dispose()
    }

    override fun handleViewEvent(event: EnterOtpContract.ViewEvent) {
        when (event) {
            is EnterOtpContract.ViewEvent.GoToHome -> goToHome()
            is EnterOtpContract.ViewEvent.GoToSyncDataScreen -> goToSyncDataScreen()
            is EnterOtpContract.ViewEvent.GoToEnterNameScreen -> goToEnterNameScreen()
            is EnterOtpContract.ViewEvent.GoToChangeNumberScreen -> gotToEnterNewNumberScreen()
            is EnterOtpContract.ViewEvent.GoBackWithError -> goBackWithError(event.error)
            is EnterOtpContract.ViewEvent.GoToAppLockAuthentication -> goToAppLockAuthentication()
            is EnterOtpContract.ViewEvent.GoToEnterMobileScreen -> gotoEnterMobileScreen()
            is EnterOtpContract.ViewEvent.SendOtpSuccess -> onOtpSent(event.requestOtpMedium)
            is EnterOtpContract.ViewEvent.GoToLogin -> gotoLogin()
            is EnterOtpContract.ViewEvent.StartTimer -> startTimer(event.retryWaitingTime)
            is EnterOtpContract.ViewEvent.ResetCompleteOtpFlow -> resetOtpFlow()
            is EnterOtpContract.ViewEvent.TooManyRequests -> tooManyRequestsSent()
        }
    }

    private fun tooManyRequestsSent() {
        longToast(getString(R.string.t_001_login_toast_resend_otp_failed))
    }

    private fun resetOtpFlow() {
        lifecycleScope.launchWhenResumed {
            gotoEnterMobileSubject.onNext(Unit)
        }
    }

    private fun startTimer(retryWaitingTime: Long) {
        AnimationUtils.fadeIn(binding.textViewTimer)
        binding.textViewTimer.visible()
        otpCountDownTimer?.cancel()

        otpCountDownTimer = object : CountDownTimer(retryWaitingTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.textViewTimer.text = "${(millisUntilFinished - retryWaitingTime) / 1000}"
            }

            override fun onFinish() {
                lifecycleScope.launchWhenResumed {
                    pushIntent(EnterOtpContract.Intent.WaitingTimeFinished)
                }
            }
        }.start()
    }

    private fun onOtpSent(requestOtpMedium: RequestOtpMedium) {
        hideSoftKeyboard()
        when (requestOtpMedium) {
            RequestOtpMedium.SMS -> longToast(getString(R.string.t_001_login_toast_resend_otp_confirm))
            RequestOtpMedium.WHATSAPP -> longToast(getString(R.string.t_001_login_toast_resend_otp_whatsapp_confirm))
            RequestOtpMedium.CALL -> longToast(getString(R.string.t_001_login_toast_resend_otp_call_confirm))
        }
    }
}
