package tech.okcredit.android.referral.ui.share

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.referral.contract.utils.ReferralVersion
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.referral.R
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.databinding.FragmentShareReferralBinding
import tech.okcredit.android.referral.ui.share.ShareReferralContract.*
import javax.inject.Inject

class ShareReferralFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "ShareReferralFragment",
        R.layout.fragment_share_referral
    ) {

    @Inject
    lateinit var deviceUtils: Lazy<DeviceUtils>

    @Inject
    lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var referralEventTracker: Lazy<ReferralEventTracker>

    private val binding: FragmentShareReferralBinding by viewLifecycleScoped(FragmentShareReferralBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.linkShareWhatsapp.setOnClickListener {
            referralEventTracker.get()
                .trackReferralScreenInteracted("WhatsApp Share", ReferralVersion.REWARDS_ON_ACTIVATION)
            referralEventTracker.get().trackShareReferral(PropertyValue.REFERRAL, ReferralVersion.REWARDS_ON_ACTIVATION)
            pushIntent(Intent.WhatsAppShare)
        }
        binding.rootView.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: State) {
        binding.progressBar.isVisible = state.showProgress

        if (!state.showNudge) {
            binding.viewNudge.isVisible = false
        } else if (!binding.viewNudge.isVisible) {
            binding.viewNudge.isVisible = true
            AnimationUtils.leftRightMotionReferralNudge(binding.viewNudge)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShareToWhatsApp -> requireActivity().startActivity(event.intent)
            is ViewEvent.ShareFailure -> shortToast(R.string.share_referral_failure)
        }
    }
}
