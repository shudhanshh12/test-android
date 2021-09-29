package `in`.okcredit.ui.reset_pwd.otp

import `in`.okcredit.R
import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.databinding.ResetpwdFragmentOtpBinding
import `in`.okcredit.ui._dialog.NetworkErrorDialog
import `in`.okcredit.ui.reset_pwd.ResetPwdActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.goodiebag.pinview.Pinview
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.KeyboardUtil
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OtpFragment : Fragment(), OtpContract.View {

    @Inject
    lateinit var viewModel: OtpContract.Presenter

    @Inject
    lateinit var smsHelper: Lazy<SmsHelper>

    private var isAutoSms = false
    private val tasks = CompositeDisposable()

    private val binding: ResetpwdFragmentOtpBinding by viewLifecycleScoped(ResetpwdFragmentOtpBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ResetpwdFragmentOtpBinding.inflate(inflater, container, false).root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)
        Analytics.track(AnalyticsEvents.RESET_PWD_OTP_SCREEN)
        binding.apply {
            otpError.visibility = View.GONE
            loadingAutoread.visibility = View.GONE
            loadingVerify.visibility = View.GONE
            verifySuccess.visibility = View.GONE
            otp.setPinViewEventListener { pinview: Pinview?, fromUser: Boolean ->
                otpError.visibility = View.GONE
                viewModel.verifyOtp(otp.value)
            }
            otpResend.setOnClickListener {
                otp.clearOtp()
                viewModel.reSendOtp()
            }
        }
        tasks.add(
            smsHelper
                .get()
                .otp()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { o: String? ->
                        binding.otp.run { setValue(o!!) }
                        isAutoSms = true
                        Analytics.track(
                            AnalyticsEvents.PASSWORD_OTP_ENTERED,
                            EventProperties
                                .create()
                                .with(PropertyKey.TYPE, if (isAutoSms) "auto" else "manual")
                        )
                    }
                ) { throwable: Throwable? ->
                    Timber.e(
                        throwable,
                        "otp auto read channel failed"
                    )
                }
        )
        binding.otp.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        smsHelper.get().startListening()
        viewModel.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        smsHelper.get().stopListening()
        viewModel.detachView()
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    override fun setMobile(m: String) {
        binding.mobile.text = m
    }

    override fun showIncorrectOtpError() {
        binding.apply {
            otpError.visibility = View.VISIBLE
            loadingAutoread.visibility = View.GONE
            loadingVerify.visibility = View.GONE
            verifySuccess.visibility = View.GONE
            otpErrorMsg.setText(R.string.register_err_incorrect_otp)
        }
    }

    override fun showExpiredOtpError() {
        binding.apply {
            otpError.visibility = View.VISIBLE
            loadingAutoread.visibility = View.GONE
            loadingVerify.visibility = View.GONE
            verifySuccess.visibility = View.GONE
            otpErrorMsg.setText(R.string.reset_pwd_otp_expired)
        }
    }

    override fun showAutoreadLoading() {
        binding.apply {
            otpError.visibility = View.GONE
            loadingAutoread.visibility = View.VISIBLE
            loadingVerify.visibility = View.GONE
            verifySuccess.visibility = View.GONE
        }
    }

    override fun setTimer(secondsRemaining: Long) {
        binding.timer.text = "$secondsRemaining seconds"
    }

    override fun showVerificationLoading() {
        binding.apply {
            otpError.visibility = View.GONE
            loadingAutoread.visibility = View.GONE
            loadingVerify.visibility = View.VISIBLE
            verifySuccess.visibility = View.GONE
        }
    }

    override fun showVerificationSuccess() {
        binding.apply {
            otpError.visibility = View.GONE
            loadingAutoread.visibility = View.GONE
            loadingVerify.visibility = View.GONE
            verifySuccess.visibility = View.VISIBLE
        }
        Observable.timer(
            1,
            TimeUnit.SECONDS,
            ThreadUtils.newThread()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<Long?>() {
                override fun onNext(aLong: Long) {
                    gotoPasswordScreen()
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
    }

    override fun hideResendButton() {
        binding.otpResend.visibility = View.GONE
    }

    override fun showResendButton() {
        binding.otpResend.visibility = View.VISIBLE
    }

    override fun gotoPasswordScreen() {
        if (activity != null) {
            (activity as ResetPwdActivity).showPasswordFragment()
        }
    }

    override fun showError() {
        Toast.makeText(
            activity,
            R.string.err_default,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showNoInternetMessage() {
        NetworkErrorDialog()
            .show(
                requireActivity(),
                object : NetworkErrorDialog.Listener {
                    override fun onNetworkOk() {
                        viewModel.onInternetRestored()
                    }

                    override fun onCancel() {
                        KeyboardUtil.hideKeyboard(this@OtpFragment)
                        activity?.onBackPressed()
                    }
                }
            )
    }

    companion object {
        const val ARG_MOBILE = "mobile"
        fun newInstance(mobile: String?): OtpFragment {
            val args = Bundle()
            args.putString(ARG_MOBILE, mobile)
            val fragment = OtpFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
