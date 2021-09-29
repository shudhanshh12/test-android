package `in`.okcredit.payment

import `in`.okcredit.payment.contract.ApiErrorType
import `in`.okcredit.payment.contract.JuspayCallbackListener
import `in`.okcredit.payment.contract.JuspayErrorType
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.databinding.ActivityJuspayPspBinding
import android.os.Bundle
import dagger.Lazy
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class PspUpiActivity : OkcActivity(false), JuspayCallbackListener {

    companion object {
        const val ARG_GATEWAY_TXN_ID = "gateway_txn_id"
        const val ARG_GATEWAY_REF_ID = "gateway_ref_id"
        const val ARG_FEATURE = "feature"
        const val ARG_FEATURE_APPROVE_COLLECT = "feature_approve_collect"
    }

    private var intentUrl: String? = null
    private var feature: String? = null
    private var gatewayTxnId: String = ""
    private var gatewayRefId: String = ""
    private val binding: ActivityJuspayPspBinding by viewLifecycleScoped(ActivityJuspayPspBinding::inflate)

    @Inject
    lateinit var paymentNavigator: Lazy<PaymentNavigator>

    override fun onCreate(savedInstanceState: Bundle?) {
        window.disableScreanCapture()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        handleIntentData()
    }

    private fun handleIntentData() {
        intent?.let { intent ->
            intent.action?.let { action ->
                if (action == "android.intent.action.VIEW")
                    intent.data?.let {
                        intentUrl = it.toString()
                    }
            }
            intent.extras?.let {
                feature = it.getString(ARG_FEATURE)
                gatewayTxnId = it.getString(ARG_GATEWAY_TXN_ID) ?: ""
                gatewayRefId = it.getString(ARG_GATEWAY_REF_ID) ?: ""
            }
        }

        when {
            intentUrl != null -> {
                processIncomingIntentSdk(intentUrl!!)
            }
            feature == ARG_FEATURE_APPROVE_COLLECT -> {
                processApproveCollect()
            }
            else -> {
                processAccountMgmt()
            }
        }
    }

    private fun processAccountMgmt() {
        paymentNavigator.get().startPspProfileManagementSdk(
            this
        )
    }

    private fun processIncomingIntentSdk(intentData: String) {
        paymentNavigator.get().startPspIncomingIntentSdk(
            this,
            intentData
        )
    }

    private fun processApproveCollect() {
        paymentNavigator.get().startApproveCollectRequestSdk(
            this,
            gatewayTxnId,
            gatewayRefId
        )
    }

    override fun onJuspaySdkOpened() {}

    override fun onJuspaySdkBackpressed() {}

    override fun onApiFailure(errorType: ApiErrorType) {}

    override fun onJuspaySdkClosed(paymentId: String, paymentType: String, amount: Long, errorType: JuspayErrorType) {
        finish()
    }

    override fun onBackPressed() {
        if (paymentNavigator.get().isBackPressed(this)) {
            return
        }
        super.onBackPressed()
    }
}
