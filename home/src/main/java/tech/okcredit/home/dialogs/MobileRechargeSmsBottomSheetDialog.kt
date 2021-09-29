package tech.okcredit.home.dialogs

import `in`.okcredit.analytics.Event.CLICKED_NOT_NOW
import `in`.okcredit.analytics.Event.CLICKED_TELL_NOW
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.service.in_app_notification.MixPanelInAppNotificationTypes.INAPP_RECHARGE_SMS
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import `in`.okcredit.web.WebExperiment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.home.databinding.BottomSheetInappRechargeSmsDialogBinding
import javax.inject.Inject

class MobileRechargeSmsBottomSheetDialog : ExpandedBottomSheetDialogFragment() {

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    companion object {
        private const val KEY_MERCHANT_ID = "merchantId"
        const val TAG = "MobileRechargeSmsBottomSheetDialog"
        fun start(fragmentManager: FragmentManager, merchantId: String) {
            val args = Bundle().apply {
                putString(KEY_MERCHANT_ID, merchantId)
            }

            val fragment = MobileRechargeSmsBottomSheetDialog().apply {
                arguments = args
            }
            fragment.show(fragmentManager, TAG)
        }
    }

    private val binding: BottomSheetInappRechargeSmsDialogBinding by viewLifecycleScoped(
        BottomSheetInappRechargeSmsDialogBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = BottomSheetInappRechargeSmsDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvYes.setOnClickListener {
            tracker.get().trackInAppDisplayedSMS(
                event = CLICKED_TELL_NOW,
                service = INAPP_RECHARGE_SMS,
                merchantId = arguments?.getString(KEY_MERCHANT_ID) ?: "",
                source = "inapp"
            )
            legacyNavigator.get()
                .goWebExperimentScreen(view.context!!, WebExperiment.Experiment.MONEY_TRANSFER_SMS.type)
            dialog?.dismiss()
        }

        binding.tvNotNow.setOnClickListener {
            tracker.get().trackInAppDisplayedSMS(
                event = CLICKED_NOT_NOW,
                service = INAPP_RECHARGE_SMS,
                merchantId = arguments?.getString(KEY_MERCHANT_ID) ?: "",
                source = "inapp"
            )
            dialog?.dismiss()
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
