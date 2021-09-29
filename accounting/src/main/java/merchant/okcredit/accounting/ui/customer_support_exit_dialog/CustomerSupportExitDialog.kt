package merchant.okcredit.accounting.ui.customer_support_exit_dialog

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.databinding.DialogCustomerSupportExitBinding
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerSupportExitDialog :
    BaseBottomSheetWithViewEvents<CustomerSupportExitContract.State, CustomerSupportExitContract.ViewEvents, CustomerSupportExitContract.Intent>(
        "CustomerSupportExitDialog"
    ) {
    private val binding: DialogCustomerSupportExitBinding by viewLifecycleScoped(DialogCustomerSupportExitBinding::bind)

    @Inject
    lateinit var firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>

    @Inject
    lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    companion object {
        const val TAG = "CustomerSupportExit"
        const val ARG_LEDGER_TYPE = "ledger_type"
        const val ARG_ACCOUNT_ID = "account_id"
        const val ARG_SOURCE = "source"

        fun newInstance(
            ledgerType: String,
            accountId: String,
            source: String,
        ): CustomerSupportExitDialog {
            val args = Bundle()
            args.putString(ARG_LEDGER_TYPE, ledgerType)
            args.putString(ARG_ACCOUNT_ID, accountId)
            args.putString(ARG_SOURCE, source)
            return CustomerSupportExitDialog().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboardStateHidden)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogCustomerSupportExitBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewCross.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.buttonSupport.clicks()
                .throttleFirst(200L, TimeUnit.MILLISECONDS)
                .map {
                    CustomerSupportExitContract.Intent.ActionClicked(getCurrentState().supportNumber)
                },
        )
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportNumber))
        startActivity(intent)
    }

    override fun render(state: CustomerSupportExitContract.State) {
        binding.buttonSupport.text =
            if (state.supportType == SupportType.CHAT)
                getString(R.string.t_002_24X7help_exit_nudge_chatCTA, getCurrentState().supportNumber).trim()
            else
                getString(R.string.t_002_24X7help_exit_nudge_callCTA, getCurrentState().supportNumber).trim()

        binding.imageViewSupport.setImageResource(
            if (state.supportType == SupportType.CHAT)
                R.drawable.ic_customer_support_chat_green
            else
                R.drawable.ic_customer_support_call_green
        )

        binding.buttonSupport.icon =
            if (state.supportType == SupportType.CHAT)
                getDrawableCompact(R.drawable.ic_whatsapp)
            else
                getDrawableCompact(R.drawable.ic_call)
    }

    override fun handleViewEvent(event: CustomerSupportExitContract.ViewEvents) {
        when (event) {
            is CustomerSupportExitContract.ViewEvents.SendWhatsAppMessage -> startActivity(event.intent)
            CustomerSupportExitContract.ViewEvents.ShowDefaultError -> shortToast(R.string.err_default)
            CustomerSupportExitContract.ViewEvents.ShowWhatsAppError -> shortToast(R.string.whatsapp_not_installed)
            CustomerSupportExitContract.ViewEvents.CallCustomerCare -> callSupport()
        }
    }

    override fun loadIntent(): UserIntent? {
        return CustomerSupportExitContract.Intent.Load
    }

    override fun onDestroyView() {
        if (isStateInitialized()) {
            getCurrentState().let { state ->
                if (state.isActionClicked.not()) {
                    accountingEventTracker.get().trackExitPopUpAction(
                        accountId = state.accountId,
                        source = state.source,
                        type = state.supportType.value,
                        relation = state.ledgerType.lowercase(),
                        supportMsg = state.supportMsg,
                        action = "dismissed",
                        supportNumber = getCurrentState().supportNumber
                    )
                }
            }
        }
        super.onDestroyView()
    }
}
