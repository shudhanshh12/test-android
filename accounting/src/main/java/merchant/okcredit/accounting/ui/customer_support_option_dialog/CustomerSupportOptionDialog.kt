package merchant.okcredit.accounting.ui.customer_support_option_dialog

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.databinding.DialogCustomerSupportOptionBinding
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import javax.inject.Inject

class CustomerSupportOptionDialog :
    BaseBottomSheetWithViewEvents<CustomerSupportOptionContract.State, CustomerSupportOptionContract.ViewEvents, CustomerSupportOptionContract.Intent>(
        "CustomerSupportOptionDialog"
    ) {
    private val binding: DialogCustomerSupportOptionBinding by viewLifecycleScoped(DialogCustomerSupportOptionBinding::bind)

    @Inject
    lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    companion object {
        const val TAG = "CustomerSupportOption"
        const val ARG_AMOUNT = "amount"
        const val ARG_PAYMENT_TIME = "payment_time"
        const val ARG_PAYMENT_ID = "payment_id"
        const val ARG_STATUS = "status"
        const val ARG_ACCOUNT_ID = "account_id"
        const val ARG_LEDGER_TYPE = "ledger_type"
        const val ARG_SOURCE = "source"

        fun newInstance(
            amount: String = "",
            paymentTime: String = "",
            txnId: String = "",
            status: String = "",
            accountId: String = "",
            ledgerType: String = "",
            source: String = "",
        ): CustomerSupportOptionDialog {
            val args = Bundle()
            args.putString(ARG_AMOUNT, amount)
            args.putString(ARG_PAYMENT_TIME, paymentTime)
            args.putString(ARG_PAYMENT_ID, txnId)
            args.putString(ARG_STATUS, status)
            args.putString(ARG_ACCOUNT_ID, accountId)
            args.putString(ARG_LEDGER_TYPE, ledgerType)
            args.putString(ARG_SOURCE, source)
            return CustomerSupportOptionDialog().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboardStateHidden)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogCustomerSupportOptionBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.textWhatsapp.setOnClickListener {
            val msg = if (getCurrentState().supportMsg.isNotNullOrBlank())
                getCurrentState().supportMsg
            else getString(R.string.t_002_i_need_help_generic)
            pushIntent(CustomerSupportOptionContract.Intent.SendWhatsAppMessage(msg))
        }

        binding.textCall.setOnClickListener {
            val msg = if (getCurrentState().supportMsg.isNotNullOrBlank())
                getCurrentState().supportMsg
            else getString(R.string.t_002_i_need_help_generic)
            pushIntent(CustomerSupportOptionContract.Intent.ActionCallClicked(msg))
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportCallNumber))
        startActivity(intent)
        dismissAllowingStateLoss()
    }

    override fun render(state: CustomerSupportOptionContract.State) {
        if (state.supportType == SupportType.CALL) {
            binding.textCall.visible()
            binding.viewMiddle.visible()
        } else {
            binding.textCall.gone()
            binding.viewMiddle.gone()
        }
    }

    override fun handleViewEvent(event: CustomerSupportOptionContract.ViewEvents) {
        when (event) {
            is CustomerSupportOptionContract.ViewEvents.SendWhatsAppMessage -> {
                startActivity(event.intent)
                dismissAllowingStateLoss()
            }
            CustomerSupportOptionContract.ViewEvents.ShowDefaultError -> shortToast(R.string.err_default)
            CustomerSupportOptionContract.ViewEvents.ShowWhatsAppError -> shortToast(R.string.whatsapp_not_installed)
            CustomerSupportOptionContract.ViewEvents.CallCustomerCare -> callSupport()
        }
    }

    override fun loadIntent(): UserIntent {
        return CustomerSupportOptionContract.Intent.Load
    }

    override fun onDestroyView() {
        if (isStateInitialized()) {
            getCurrentState().let { state ->
                if (state.isActionClicked.not()) {
                    accountingEventTracker.get().trackLedgerPopUpAction(
                        accountId = state.accountId,
                        type = state.supportType.value,
                        txnId = state.txnId,
                        amount = state.amount,
                        relation = state.ledgerType,
                        supportMsg = state.supportMsg,
                        action = "dismissed",
                        supportNumber = if (state.supportType == SupportType.CALL) state.supportCallNumber else state.supportChatNumber,
                        source = state.source,
                    )
                }
            }
        }
        super.onDestroyView()
    }
}
