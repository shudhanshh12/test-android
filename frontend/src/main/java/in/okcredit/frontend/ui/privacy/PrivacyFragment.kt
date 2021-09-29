package `in`.okcredit.frontend.ui.privacy

import `in`.okcredit.backend.contract.Constants
import `in`.okcredit.frontend.R
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import com.google.firebase.perf.metrics.AddTrace
import io.reactivex.Observable
import kotlinx.android.synthetic.main.privacy_fragment.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import javax.inject.Inject

class PrivacyFragment : BaseScreen<PrivacyContract.State>("PrivacyScreen"), PrivacyContract.Navigator {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.privacy_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root_view.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return PrivacyContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    @AddTrace(name = Traces.RENDER_PRIVACY)
    override fun render(state: PrivacyContract.State) {
        screen_title.text = getString(R.string.privacy_policy)
        bottom_text.text = Html.fromHtml(context?.getString(R.string.click_to_know_more))

        bottom_text.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(Constants.PRIVACY_POLICY_URL)
                startActivity(intent)
            } catch (e: Exception) {}
        }
    }

    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }
}
