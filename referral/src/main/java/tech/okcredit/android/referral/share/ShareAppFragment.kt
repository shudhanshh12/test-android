package tech.okcredit.android.referral.share

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import androidx.core.view.isVisible
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.databinding.FragmentShareAppBinding
import tech.okcredit.android.referral.share.ShareAppContract.Intent
import tech.okcredit.android.referral.share.ShareAppContract.State
import tech.okcredit.android.referral.share.ShareAppContract.ViewEvent
import javax.inject.Inject

class ShareAppFragment : BaseFragment<State, ViewEvent, Intent>(
    "ShareAppFragment",
    R.layout.fragment_share_app
) {

    private val binding: FragmentShareAppBinding by viewLifecycleScoped(FragmentShareAppBinding::bind)

    @Inject
    internal lateinit var tracker: Lazy<ReferralEventTracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracker.get().trackShareAppViewed()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.shareOnWhatsapp.clicks().map {
                tracker.get().trackSharedOnWhatsApp()
                tracker.get().trackShareAppInteracted("Share on WhatsApp")
                Intent.ShareOnWhatsApp
            }
        )
    }

    override fun render(state: State) {
        binding.referralDescription.isVisible = state.canShowReferralDescription
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShareApp -> startActivity(event.value)

            is ViewEvent.ShareAppFailure -> shortToast(R.string.share_app_failure)
        }
    }
}
