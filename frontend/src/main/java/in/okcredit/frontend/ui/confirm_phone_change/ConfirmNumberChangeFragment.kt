package `in`.okcredit.frontend.ui.confirm_phone_change

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.google.firebase.perf.metrics.AddTrace
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.confirm_number_change_fragment.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConfirmNumberChangeFragment :
    BaseScreen<ConfirmNumberChangeContract.State>("ConfirmNumberChangeScreen"),
    ConfirmNumberChangeContract.Navigator {

    companion object {
        const val NUMBER_CHANGE = 1
    }

    override fun goToOTPVerificationScreen(mobile: String) {
        if (this.activity != null)
            startActivityForResult(
                MainActivity.startingIntentForEnterOtpScreen(
                    requireActivity(),
                    mobile,
                    OnboardingConstants.FLAG_VERIFY_NEW_NUMBER_AND_CHANGE_NUMBER,
                    true
                ),
                NUMBER_CHANGE
            )
    }

    private val verifyAndChangeClicked: PublishSubject<Unit> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.confirm_number_change_fragment, container, false)
    }

    override fun loadIntent(): UserIntent {
        return ConfirmNumberChangeContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            verifyAndChangeClicked.throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    ConfirmNumberChangeContract.Intent.VerfiyAndChange
                }
        )
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_CONFIRM_PHONE_CHANGE)
    override fun render(state: ConfirmNumberChangeContract.State) {
        btn_verify_and_change.setOnClickListener {
            tracker.trackConfirmNumberChange(PropertyValue.NUMBER_CHANGE, PropertyValue.NEW, PropertyValue.TRUE)
            verifyAndChangeClicked.onNext(Unit)
        }

        oldnumber_to_new_number.text = convertOldNumberToNewNumberString(
            oldNumber = state.business?.mobile.toString(),
            newNumber = state.tempNewNumber.toString()
        )

        line_3.text =
            Html.fromHtml(context?.getString(R.string.confirm_number_change_line3, state.tempNewNumber.toString()))
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            requireActivity().finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == NUMBER_CHANGE) {
                activity?.setResult(RESULT_OK, data)
                activity?.finish()
            }
        }
    }

    private fun convertOldNumberToNewNumberString(oldNumber: String, newNumber: String) =
        Html.fromHtml("<b>$oldNumber</b>" + getString(R.string.to) + "<b>$newNumber</b>")
}
