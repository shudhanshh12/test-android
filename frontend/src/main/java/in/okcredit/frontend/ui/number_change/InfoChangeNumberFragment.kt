package `in`.okcredit.frontend.ui.number_change

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.onboarding.contract.OnboardingConstants
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.google.firebase.perf.metrics.AddTrace
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.number_change_fragment.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InfoChangeNumberFragment :
    BaseScreen<InfoChangeNumberContract.State>("InfoChangeNumberFragment"),
    InfoChangeNumberContract.Navigator {
    override fun goToOTPVerificationScreen(mobile: String) {
        if (isAdded) {
            startActivity(
                MainActivity.startingIntentForEnterOtpScreen(
                    requireActivity(),
                    mobile,
                    OnboardingConstants.FLAG_NUMBER_CHANGE,
                    false
                )
            )
            requireActivity().finish()
        }
    }

    private val verifyNumberClicked: PublishSubject<Unit> = PublishSubject.create()

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.number_change_fragment, container, false)
    }

    override fun loadIntent(): UserIntent {
        return InfoChangeNumberContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            verifyNumberClicked.throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    InfoChangeNumberContract.Intent.VerifyAndChange
                }
        )
    }

    @SuppressLint("RestrictedApi")
    @AddTrace(name = Traces.RENDER_NUMBER_CHANGE)
    override fun render(state: InfoChangeNumberContract.State) {
        btn_verify_number.setOnClickListener {
            tracker.trackConfirmNumberChange(PropertyValue.NUMBER_CHANGE, PropertyValue.OLD, null)
            verifyNumberClicked.onNext(Unit)
        }
        number_change_line_3_tv.text = Html.fromHtml(getString(R.string.change_number_tv_3, state.mobile))
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            requireActivity().finish()
        }
    }
}
