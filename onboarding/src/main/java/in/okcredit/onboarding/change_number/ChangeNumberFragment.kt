package `in`.okcredit.onboarding.change_number

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import com.google.firebase.perf.metrics.AddTrace
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.change_number_fragment.*
import tech.okcredit.android.base.utils.MobileUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChangeNumberFragment :
    BaseScreen<ChangeNumberContract.State>("ChangeNumberFragment"),
    ChangeNumberContract.Navigator {

    companion object {
        private const val NUMBER_CHANGE = 1
    }

    private lateinit var newNumber: String

    private val newNumberEntered: PublishSubject<String> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    override fun goToChangeNumberConfirmationScreen() {
        legacyNavigator.goToPhoneNumberChangeConfirmationScreen(this.requireActivity(), newNumber, NUMBER_CHANGE)
    }

    override fun goToOTPVerificationScreen(mobile: String) {
        if (this.activity != null) {
            legacyNavigator.goToOtpScreen(
                this.requireActivity(),
                mobile,
                OnboardingConstants.FLAG_NUMBER_CHANGE,
                false
            )
        }
    }

    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.change_number_fragment, container, false)
    }

    override fun loadIntent(): UserIntent {
        return ChangeNumberContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            newNumberEntered.throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    ChangeNumberContract.Intent.NewNumberEntered(it)
                }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etNumberChange.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        etNumberChange.clearFocus()
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_ENTER_NEW_NUMBER)
    override fun render(state: ChangeNumberContract.State) {

        clSubmit.setOnClickListener {
            tracker.requestOTP("Number Change", "Mobile Update")
            newNumber = etNumberChange.text.toString()
            if (!MobileUtils.isPhoneNumberValid(newNumber)) {
                Toast.makeText(context, getString(R.string.invalid_mobile), Toast.LENGTH_SHORT).show()
            } else {
                newNumberEntered.onNext(newNumber)
            }
        }

        etNumberChange.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length == 10) {
                    clSubmit.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_circle_green_filled_chevron))
                } else {
                    clSubmit.setImageDrawable(context?.resources?.getDrawable(R.drawable.ic_circle_filled_chevron))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                error_text.visibility = View.GONE
            }
        })
        if (state.merchantAlreadyExistsError) {
            error_text.visibility = View.VISIBLE
        } else {
            error_text.visibility = View.GONE
        }

        if (state.isLoading) {
            clSubmit.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            clSubmit.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            requireActivity().finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NUMBER_CHANGE) {
                if (data != null) {
                    val error = data.getStringExtra("error")
                    if (error != "merchant_exists" || error != "user_exists") {
                        activity?.finish()
                    }
                }
            }
        }
    }
}
