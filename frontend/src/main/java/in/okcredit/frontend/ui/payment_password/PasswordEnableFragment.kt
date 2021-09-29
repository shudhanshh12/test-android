package `in`.okcredit.frontend.ui.payment_password

import `in`.okcredit.analytics.Analytics
import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.EventProperties
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.utils.DrawableUtil
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.password_enable_fragment.*
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.contract.AppLock
import tech.okcredit.contract.AppLockTracker
import tech.okcredit.contract.Constants.IS_AUTHENTICATED
import tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED
import tech.okcredit.contract.Constants.SECURITY_PIN_SET
import tech.okcredit.contract.OnSetPinClickListener
import tech.okcredit.contract.OnUpdatePinClickListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PasswordEnableFragment :
    BaseScreen<PasswordEnableContract.State>("PasswordEnableScreen"),
    PasswordEnableContract.Navigator,
    OnUpdatePinClickListener,
    OnSetPinClickListener {

    private val onChangeInputMode: PublishSubject<Boolean> = PublishSubject.create()

    // isPasswordVisible is using for setting password visibility
    private var isPasswordVisible: Boolean = false
    private var alert: Snackbar? = null

    private val compositeDisposable = CompositeDisposable()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var appLock: Lazy<AppLock>

    @Inject
    internal lateinit var appLockTracker: Lazy<AppLockTracker>

    private val submitPassword: PublishSubject<Boolean> = PublishSubject.create()
    private val btnClickSubject: PublishSubject<UserIntent> = PublishSubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.password_enable_fragment, container, false)
    }

    override fun loadIntent(): UserIntent {
        return PasswordEnableContract.Intent.Load
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        root_view.setTracker(performanceTracker)
    }

    private fun initListeners() {
        btn_submit.setOnClickListener {
            if (getCurrentState().isPasswordSet) {
                if (getCurrentState().isMerchantSync) {
                    if (getCurrentState().isFourDigitPin)
                        btnClickSubject
                            .onNext(PasswordEnableContract.Intent.ChangePasswordEnableStatus(true))
                    else
                        btnClickSubject.onNext(PasswordEnableContract.Intent.CheckIsFourDigit)
                } else {
                    btnClickSubject.onNext(PasswordEnableContract.Intent.SyncMerchantPref)
                }
            } else
                btnClickSubject.onNext(PasswordEnableContract.Intent.SetNewPinClicked)
        }
    }

    companion object {
        const val PAYMENT_PASSWORD = 161
        const val PAYMENT_PASSWORD_SET_NEW_PIN = 16001
        const val PAYMENT_PASSWORD_UPDATE_PIN = 16002
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            btnClickSubject.throttleLast(300, TimeUnit.MILLISECONDS),

            submitPassword
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    PasswordEnableContract.Intent.SubmitPassword(it)
                },

            btn_forgot_password.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    PasswordEnableContract.Intent.OnForgotPasswordClicked
                },

            onChangeInputMode
                .map { PasswordEnableContract.Intent.ChangePasswordEnableStatus(it) }
        )
    }

    @AddTrace(name = Traces.RENDER_PAYMENT_PASSWORD)
    override fun render(state: PasswordEnableContract.State) {

        try {
            KeyboardVisibilityEvent.setEventListener(requireContext() as Activity) { isOpen ->
                if (!isOpen) {
                    AnimationUtils.fadeOut(dimLayout)
                    compositeDisposable.add(
                        Completable
                            .timer(300, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { dimLayout.visibility = View.GONE }
                    )
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dimLayout.visibility = View.VISIBLE
                        AnimationUtils.fadeIn(dimLayout)
                    }
                }
            }
        } catch (e: java.lang.Exception) {
        }

        bottom_container_right_icon.setOnClickListener {
            if (isPasswordVisible) {
                bottom_container_text.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                bottom_container_right_icon.setImageResource(R.drawable.ic_remove_eye)
            } else {
                bottom_container_text.inputType = InputType.TYPE_CLASS_NUMBER
                bottom_container_right_icon.setImageResource(R.drawable.ic_eye_off)
            }
            bottom_container_text.post {
                bottom_container_text.text?.length?.let { it1 ->
                    bottom_container_text.setSelection(
                        it1
                    )
                }
            }
            isPasswordVisible = !isPasswordVisible
        }

        if (state.isPasswordEnable) {
            lock_image_main.setImageDrawable(
                DrawableUtil.getDrawableWithAttributeColor(
                    requireContext(),
                    R.drawable.ic_lock,
                    R.attr.colorPrimary
                )
            )
            title.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))

            btn_image.setImageResource(R.drawable.ic_lock_open)
            title.text = getString(R.string.password_enabled)
            description.text = getString(R.string.payment_password_protects)
            btn_title.text = getString(R.string.disable)
        } else {
            lock_image_main.setImageDrawable(
                DrawableUtil.getDrawableWithColor(
                    requireContext(),
                    R.drawable.ic_lock_open,
                    R.color.grey600
                )
            )
            title.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey700))

            btn_image.setImageResource(R.drawable.ic_lock)
            title.text = getString(R.string.password_disabled)
            description.text = getString(R.string.for_addn_payment)
            btn_title.text = getString(R.string.enable)
        }
        if (!state.isEnterPasswordMode) {
            hideSoftKeyboard()
            bottom_container_text.text = null

            bottom_text_container.visibility = View.GONE
            btn_forgot_password.visibility = View.GONE
            btn_submit.visibility = View.VISIBLE
        }

        if (state.isIncorrectPassword) {
            bottom_container_text.setError(getString(R.string.txn_incorrect_password), null)

            btn_forgot_password.strokeColor = requireContext().getColorFromAttr(R.attr.colorPrimary)
            forgot_password_icon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary))
            forgot_password_text.setTextColor(requireContext().getColorFromAttr(R.attr.colorPrimary))
        } else {
            bottom_container_text.error = null
        }

        if (state.loader) {
            loader.visibility = View.VISIBLE
            btn_submit_password.hide()
        } else {
            loader.visibility = View.GONE
            btn_submit_password.show()
        }

        // show/hide alert
        if (state.networkError) {
            alert = view?.snackbar(getString(R.string.home_no_internet_msg), Snackbar.LENGTH_INDEFINITE)
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    override fun onBackPressed(): Boolean {
        if (getCurrentState().isEnterPasswordMode) {
            onChangeInputMode.onNext(false)
        } else {
            requireActivity().finish()
        }
        return true
    }

    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    @UiThread
    override fun gotoForgotPasswordScreen(mobile: String) {
        activity?.runOnUiThread {
            legacyNavigator.goToForgotPasswordScreen(requireActivity(), mobile)
        }
    }

    override fun gotoEnterPinScreen() {
        requireActivity().runOnUiThread {
            startActivityForResult(
                appLock.get()
                    .appLock(getString(R.string.enterpin_screen_deeplink), requireActivity(), "PasswordEnableScreen"),
                PAYMENT_PASSWORD
            )
        }
    }

    override fun showUpdatePinDialog() {
        appLock.get().showUpdatePin(requireActivity().supportFragmentManager, this, PAYMENT_PASSWORD_UPDATE_PIN)
    }

    override fun showSetNewPinDialog() {
        appLock.get().showSetNewPin(requireActivity().supportFragmentManager, this, PAYMENT_PASSWORD_SET_NEW_PIN)
    }

    override fun checkFourDigitPin(isFourDigit: Boolean) {
        if (isFourDigit)
            btnClickSubject
                .onNext(PasswordEnableContract.Intent.ChangePasswordEnableStatus(true))
        else
            btnClickSubject.onNext(PasswordEnableContract.Intent.UpdatePinClicked)
    }

    override fun syncDone() {
        btnClickSubject.onNext(PasswordEnableContract.Intent.CheckIsFourDigit)
    }

    override fun goBack() {
        requireActivity().finish()
    }

    private fun goToSetPinScreen(requestCode: Int) {
        startActivityForResult(
            appLock.get()
                .appLock(getString(R.string.changepin_screen_deeplink), requireActivity(), "PasswordEnableScreen"),
            requestCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_PASSWORD ||
            requestCode == PAYMENT_PASSWORD_SET_NEW_PIN ||
            requestCode == PAYMENT_PASSWORD_UPDATE_PIN
        ) {
            data?.let {
                if (it.getBooleanExtra(IS_AUTHENTICATED, false)) {
                    if (requestCode == PAYMENT_PASSWORD_SET_NEW_PIN)
                        appLockTracker.get().trackEvents(eventName = SECURITY_PIN_SET, source = "PasswordEnableScreen")
                    if (requestCode == PAYMENT_PASSWORD_UPDATE_PIN)
                        appLockTracker.get()
                            .trackEvents(eventName = SECURITY_PIN_CHANGED, source = "PasswordEnableScreen")
                    submitPassword()
                }
            }
        }
    }

    private fun submitPassword() {
        Analytics.track(
            AnalyticsEvents.UPDATE_PASSWORD_SETTING,
            EventProperties
                .create()
                .with(PropertyKey.SOURCE, "default")
                .with(
                    PropertyKey.TYPE,
                    if (btn_title.text == getString(R.string.enable)) "enable" else "disable"
                )
        )
        Completable
            .timer(100, TimeUnit.MILLISECONDS)
            .observeOn(schedulerProvider.get().ui())
            .subscribe {
                submitPassword.onNext(btn_title.text == getString(R.string.enable))
            }.addTo(autoDisposable)
    }

    override fun onSetNewPinClicked(requestCode: Int) {
        goToSetPinScreen(requestCode)
    }

    override fun onUpdateDialogDismissed() {
    }

    override fun onSetPinClicked(requestCode: Int) {
        goToSetPinScreen(requestCode)
    }

    override fun onDismissed() {
    }
}
