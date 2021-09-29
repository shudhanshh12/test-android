package `in`.okcredit.onboarding.otp_verification

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.onboarding.BuildConfig
import `in`.okcredit.onboarding.R
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.SmsHelper
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.transition.Explode
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.airbnb.lottie.RenderMode
import com.goodiebag.pinview.Pinview
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.otp_verification_fragment.*
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.utils.KeyboardUtil
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A Screen to verify mobile number through otp  of an authenticated merchant.
 * <p>
 * sending opt when user opening the screen, and passing after verifying passing data through <code>onActivityResult</code>
 * Flag is used for analytics, its passing through intent.
 * Activity.RESULT_OK for success and Activity.RESULT_CANCELLED.
 * <p>
 */

class OtpVerificationFragment :
    BaseScreen<OtpVerificationContract.State>("OtpVerificationScreen"),
    OtpVerificationContract.Navigator {

    companion object {
        const val DELETE_UPI_FLAG = 101
        const val UPDATE_UPI_FLAG = 102
    }

    private var alert: Snackbar? = null
    private lateinit var otp: Pinview

    private val sendOtpPublishSubject: PublishSubject<Pair<Boolean, String>> = PublishSubject.create()
    private val resendOtpPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var smsHelper: SmsHelper

    private val tasks = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.otp_verification_fragment, container, false)
        otp = view.findViewById(R.id.otp)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root_view.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return OtpVerificationContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            sendOtpPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Timber.d("<<<<OTP ${it.first},  ${it.second}")
                    OtpVerificationContract.Intent.VerifyOtp(it.first, it.second)
                },

            resendOtpPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    OtpVerificationContract.Intent.ResendOtp
                },

            otp_resend
                .clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    OtpVerificationContract.Intent.ResendOtp
                }
        )
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_OTP_VERIFICATION)
    override fun render(state: OtpVerificationContract.State) {

        otp.setPinViewEventListener { _, _ ->
            sendOtpPublishSubject.onNext(false to otp.value)
        }

        title.setOnClickListener {
            if (BuildConfig.DEBUG) {
                sendOtpPublishSubject.onNext(true to "000000")
                otp.value = "000000"
            }
        }

        otp.setOnClickListener {
            KeyboardVisibilityEvent.showKeyboard(context, null, root_view)
        }

        try {
            KeyboardVisibilityEvent.setEventListener(requireActivity()) { isOpen ->
                val transitionSet = TransitionSet().addTransition(Explode()).addTransition(Fade())

                if (isOpen) {
                    TransitionManager.beginDelayedTransition(bottom_container, transitionSet)
                    animation_view.visibility = View.GONE
                } else {
                    TransitionManager.beginDelayedTransition(bottom_container, transitionSet)
                    animation_view.visibility = View.VISIBLE
                }
            }
        } catch (e: java.lang.Exception) {
        }

        desc.text = getString(R.string.your_otp_will_be_delivered, state.mobile)

        if (state.isLoading) {
            loader.visibility = View.VISIBLE
        } else {
            loader.visibility = View.GONE
        }

        if (state.isShowResendOtp) {
            if (!state.sendOtpLoader) {
                otp_resend.visibility = View.VISIBLE
            } else {
                otp_resend.visibility = View.GONE
            }
        } else {
            otp_resend.visibility = View.GONE
        }

        if (state.otpError) {
            otp.clearOtp()
            if (!state.sendOtpLoader) {
                otp_resend.visibility = View.VISIBLE
            } else {
                otp_resend.visibility = View.GONE
            }
            otp_error.visibility = View.VISIBLE
        } else {
            otp_error.visibility = View.GONE
        }

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
    }

    override fun onResume() {
        super.onResume()

        KeyboardUtil.hideKeyboard(activity)

        animation_view.setRenderMode(RenderMode.HARDWARE)
        animation_view.enableMergePathsForKitKatAndAbove(true)

        // its used for Auto reading the sms
        tasks.add(
            smsHelper
                .otp()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { o ->
                        var source = ""
                        if (getCurrentState().flag == UPDATE_UPI_FLAG) {
                            source = "update_upi"
                        } else if (getCurrentState().flag == DELETE_UPI_FLAG) {
                            source = "delete_upi"
                        }

                        Analytics.track(
                            AnalyticsEvents.AUTO_VERIFY_OTP,
                            EventProperties
                                .create()
                                .with(PropertyKey.SOURCE, source)
                        )
                        sendOtpPublishSubject.onNext(true to o)
                        otp.value = o
                    },
                    { throwable -> Timber.e(throwable, "otp auto read channel failed") }
                )
        )

        smsHelper.startListening()
    }

    override fun onPause() {
        super.onPause()

        smsHelper.stopListening()
        if (!tasks.isDisposed) {
            tasks.dispose()
        }
    }

    // sending RESULT_CANCELED on back press
    override fun onBackPressed(): Boolean {
        val backIntent = Intent()
        activity?.setResult(RESULT_CANCELED, backIntent)
        activity?.finish()
        return true
    }

    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            requireActivity().finish()
        }
    }

    override fun goHome() {
        activity?.runOnUiThread {
            legacyNavigator.goToHome(requireActivity())
            activity?.finishAffinity()
        }
    }

    // Sending back token to request Screen
    override fun goBackWithSuccessResult() {
        activity?.runOnUiThread {
            val resultIntent = Intent()
            activity?.setResult(Activity.RESULT_OK, resultIntent)
            activity?.finish()
        }
    }
}
