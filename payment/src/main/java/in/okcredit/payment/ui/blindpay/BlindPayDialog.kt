package `in`.okcredit.payment.ui.blindpay

import `in`.okcredit.payment.R
import `in`.okcredit.payment.contract.BlindPayListener
import `in`.okcredit.payment.contract.PaymentType
import `in`.okcredit.payment.databinding.DialogBlindPayBinding
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import io.reactivex.Observable
import merchant.okcredit.accounting.contract.model.SupportType
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.setGroupOnClickListener
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible

class BlindPayDialog :
    BaseBottomSheetWithViewEvents<BlindPayContract.State, BlindPayContract.ViewEvents, BlindPayContract.Intent>(
        "BlindPayDialog"
    ) {

    private val binding: DialogBlindPayBinding by viewLifecycleScoped(DialogBlindPayBinding::bind)
    private var blindPayListener: BlindPayListener? = null

    companion object {
        const val TAG = "BlindPayDialog"
        const val ARG_LEDGER_TYPE = "ledger_type"
        const val ARG_ACCOUNT_ID = "account_id"

        fun newInstance(
            ledgerType: String,
            accountId: String,
        ): BlindPayDialog {
            val bundle = Bundle()
            bundle.apply {
                putString(ARG_LEDGER_TYPE, ledgerType)
                putString(ARG_ACCOUNT_ID, accountId)
            }
            return BlindPayDialog().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogThemeWithKeyboardStateHidden)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogBlindPayBinding.inflate(inflater, container, false).root
    }

    fun setListener(listener: BlindPayListener) {
        this.blindPayListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gpEasyPay.setGroupOnClickListener {
            blindPayListener?.onPaymentTypeSelected(PaymentType.BLIND_PAY)
            dismissAllowingStateLoss()
        }

        binding.gpOthersPay.setGroupOnClickListener {
            blindPayListener?.onPaymentTypeSelected(PaymentType.OTHERS)
            dismissAllowingStateLoss()
        }

        binding.buttonSupport.setOnClickListener {
            pushIntent(
                BlindPayContract.Intent.SupportClicked(
                    getString(R.string.t_002_i_need_help_generic),
                    getCurrentState().supportNumber
                )
            )
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    private fun callSupport() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.call_template, getCurrentState().supportNumber))
        startActivity(intent)
    }

    override fun render(state: BlindPayContract.State) {
        setTopBannerUi(state.supportType)
    }

    private fun setTopBannerUi(supportType: SupportType) {
        when (supportType) {
            SupportType.CALL -> setSupportBannerUi(supportType)
            SupportType.CHAT -> setSupportBannerUi(supportType)
            SupportType.NONE -> {
                binding.apply {
                    buttonSupport.gone()
                }
            }
        }
    }

    private fun setSupportBannerUi(supportType: SupportType) {
        binding.apply {
            buttonSupport.visible()

            buttonSupport.text =
                if (supportType == SupportType.CALL) {
                    getString(
                        R.string.t_002_24X7help_banner_call_label,
                        getCurrentState().support24x7String,
                        getCurrentState().supportNumber
                    ).trim()
                } else
                    getString(
                        R.string.t_002_24X7help_banner_whatsapp_label,
                        getCurrentState().support24x7String,
                        getCurrentState().supportNumber
                    ).trim()

            buttonSupport.icon =
                getDrawableCompact(
                    if (supportType == SupportType.CALL)
                        R.drawable.ic_call_support_indigo
                    else R.drawable.ic_whatsapp_indigo
                )
        }
    }

    override fun handleViewEvent(event: BlindPayContract.ViewEvents) {
        when (event) {
            BlindPayContract.ViewEvents.CallCustomerCare -> callSupport()
            is BlindPayContract.ViewEvents.SendWhatsAppMessage -> startActivity(event.intent)
            BlindPayContract.ViewEvents.ShowDefaultError -> shortToast(getString(R.string.err_default))
            BlindPayContract.ViewEvents.ShowWhatsAppError -> shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    override fun loadIntent(): UserIntent? {
        return BlindPayContract.Intent.Load
    }
}
