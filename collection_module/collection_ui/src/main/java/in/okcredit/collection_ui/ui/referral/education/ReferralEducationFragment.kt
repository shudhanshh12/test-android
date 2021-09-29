package `in`.okcredit.collection_ui.ui.referral.education

import `in`.okcredit.collection.contract.CollectionEventTracker
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.databinding.ReferralEducationFragmentBinding
import `in`.okcredit.collection_ui.ui.referral.NavigationListener
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReferralEducationFragment :
    BaseFragment<ReferralEducationContract.State, ReferralEducationContract.ViewEvents, ReferralEducationContract.Intent>(
        "ReferralEducationFragment",
        R.layout.referral_education_fragment
    ) {

    private var listener: NavigationListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationListener) {
            listener = context
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private val binding: ReferralEducationFragmentBinding by viewLifecycleScoped(ReferralEducationFragmentBinding::bind)

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.mbInvite.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ReferralEducationContract.Intent.InviteClicked
                },
            binding.ivHelp.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ReferralEducationContract.Intent.HelpClicked
                },
        )
    }

    override fun render(state: ReferralEducationContract.State) {
    }

    override fun handleViewEvent(event: ReferralEducationContract.ViewEvents) {
        when (event) {
            ReferralEducationContract.ViewEvents.GotoReferralListScreen -> {
                listener?.moveToReferralInvite(getCurrentState().customerIdFrmLedger)
            }
            is ReferralEducationContract.ViewEvents.HelpClicked -> helpClicked(event.ids)
            is ReferralEducationContract.ViewEvents.InviteOnWhatsApp -> startActivity(event.intent)
            ReferralEducationContract.ViewEvents.ShowWhatsappError -> shortToast(
                requireContext()
                    .getString(R.string.whatsapp_not_installed)
            )
            ReferralEducationContract.ViewEvents.ShowError -> shortToast(
                requireContext().getString(R.string.err_default)
            )
        }
    }

    override fun loadIntent(): UserIntent {
        return ReferralEducationContract.Intent.Load
    }

    private fun helpClicked(contextualHelpIds: List<String>) {
        if (contextualHelpIds.isNotEmpty()) {
            legacyNavigator.get().goToHelpV2Screen(
                requireContext(), contextualHelpIds,
                source = if (getCurrentState().customerIdFrmLedger.isNotNullOrBlank()) CollectionEventTracker.CUSTOMER_SCREEN else CollectionTracker.CollectionScreen.MERCHANT_DESTINATION_SCREEN,
            )
        }
    }
}
