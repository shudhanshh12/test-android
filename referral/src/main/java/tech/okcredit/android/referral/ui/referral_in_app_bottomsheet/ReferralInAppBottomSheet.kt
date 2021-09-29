package tech.okcredit.android.referral.ui.referral_in_app_bottomsheet

import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.referral.analytics.ReferralEventTracker
import tech.okcredit.android.referral.databinding.ReferralInappBottomSheetBinding
import tech.okcredit.android.referral.ui.ReferralActivity
import javax.inject.Inject

class ReferralInAppBottomSheet : BaseBottomSheetWithViewEvents<ReferralInAppContract.State,
    ReferralInAppContract.ViewEvent,
    ReferralInAppContract.Intent>("ReferralInAppBottomSheet") {

    private val binding: ReferralInappBottomSheetBinding by viewLifecycleScoped(ReferralInappBottomSheetBinding::bind)

    @Inject
    internal lateinit var referralEventTracker: Lazy<ReferralEventTracker>

    @Inject
    internal lateinit var referralRepository: Lazy<ReferralRepository>

    companion object {
        const val TAG = "ReferralInAppBottomSheet"

        fun show(fragmentManager: FragmentManager) {
            ReferralInAppBottomSheet().show(fragmentManager, TAG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ReferralInappBottomSheetBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        referralEventTracker.get().trackReferralInApp(ReferralEventTracker.VIEW_REFERRAL)
        initClickListener()
    }

    private fun initClickListener() {
        binding.mbShareOnWhatsapp.setOnClickListener {
            referralEventTracker.get().trackReferralInApp(ReferralEventTracker.REFERRAL_SHARE)
            ReferralActivity.start(requireContext())
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun render(state: ReferralInAppContract.State) {}

    override fun loadIntent() = ReferralInAppContract.Intent.Load

    override fun handleViewEvent(event: ReferralInAppContract.ViewEvent) {}
}
