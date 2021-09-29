package `in`.okcredit.payment

import `in`.okcredit.payment.contract.ApiErrorType
import `in`.okcredit.payment.contract.EditDestinationListener
import `in`.okcredit.payment.contract.JuspayCallbackListener
import `in`.okcredit.payment.contract.JuspayErrorType
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.contract.PaymentResultListener
import `in`.okcredit.payment.databinding.ActivityPaymentBinding
import `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.PaymentEditAmountBottomSheet
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorType
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import dagger.Lazy
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.findFragmentById
import tech.okcredit.android.base.extensions.navigate
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class PaymentActivity :
    OkcActivity(shouldCallDelegate = false),
    JuspayCallbackListener,
    PaymentEditAmountBottomSheet.PaymentEditAmountBottomSheetListener,
    PaymentResultListener {

    companion object {
        const val ARG_PAYMENT_ACCOUNT_ID = "account_id"
        const val ARG_MERCHANT_ID_EDT_PAGE = "merchant_id"
        const val ARG_MAX_DAILY_LIMIT_EDT_PAGE = "max_daily_limit"
        const val ARG_REMAINING_DAILY_LIMIT_EDT_PAGE = "remaining_daily_limit"
        const val ARG_ACCOUNT_BALANCE = "account_balance"
        const val ARG_PAYMENT_RISK_TYPE = "risk_type"
        const val ARG_PAYMENT_LINK_ID = "link_id"
        const val ARG_PAYMENT_MOBILE = "mobile"
        const val ARG_PAYMENT_PAYMENT_ADDRESS = "payment_address"
        const val ARG_PAYMENT_DESTINATION_TYPE = "destination_type"
        const val ARG_PAYMENT_NAME = "name"
        const val ARG_PAYMENT_PROFILE_IMAGE = "profile_image"
        const val ARG_ACCOUNT_TYPE = "account_type"
        const val ARG_KYC_STATUS = "kyc_status"
        const val ARG_KYC_RISK_CATEGORY = "kyc_risk_category"
        const val ARG_FUTURE_AMOUNT_LIMIT = "future_amount_limit"
        const val ARG_BLIND_PAY_ENABLED = "blind_pay_enabled"
        const val ARG_BLIND_PAY_FLOW = "blind_pay_flow"
        const val ARG_PROFILE_NAME = "profile_name"
        const val ARG_DESTINATION_UPDATE_ALLOWED = "destination_update_allowed"
        const val ARG_SUPPORT_TYPE = "support_type"

        const val PAYMENT_START_SCREEN = "payment_start_screen"
        const val PAYMENT_JUSPAY_SCREEN = 1
        const val PAYMENT_BLIND_PAY_FLOW = 2

        internal var editDestinationListener: EditDestinationListener? = null

        @JvmStatic
        fun newInstance(context: Context, listener: EditDestinationListener): Intent {
            editDestinationListener = listener
            return Intent(context, PaymentActivity::class.java)
        }
    }

    @Inject
    lateinit var paymentNavigator: Lazy<PaymentNavigator>

    private var onDismissActivityShouldExit = true

    private lateinit var navHostFragment: NavHostFragment

    private val binding: ActivityPaymentBinding by viewLifecycleScoped(ActivityPaymentBinding::inflate)

    private var supplierId: String = ""
    private var riskType = ""
    private var ongoingPaymentAmount = 0L
    private var dueBalance = 0L
    private var remainingDailyLimit = 0L
    private var linkId = ""
    private var paymentAddress = ""
    private var destinationType = ""
    private var name = ""
    private var mobile = ""
    private var accountType = ""
    private var blindPayFlow = false
    private var profileName = ""
    private var kycStatus = ""
    private var kycRiskCategory = ""
    private var futureAmountLimit = 0L
    private var supportType: String = ""
    private var destinationUpdateAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        window.disableScreanCapture()
        super.onCreate(null)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(binding.root)
        handleExtras()
        initJuspaySdk()
        setUpNavigation()
    }

    private fun handleExtras() {
        intent?.let {
            supplierId = it.getStringExtra(ARG_PAYMENT_ACCOUNT_ID) ?: ""
            riskType = it.getStringExtra(ARG_PAYMENT_RISK_TYPE) ?: ""
            dueBalance = it.getLongExtra(ARG_ACCOUNT_BALANCE, 0L)
            remainingDailyLimit = it.getLongExtra(ARG_REMAINING_DAILY_LIMIT_EDT_PAGE, 0L)
            linkId = it.getStringExtra(ARG_PAYMENT_LINK_ID) ?: ""
            paymentAddress = it.getStringExtra(ARG_PAYMENT_PAYMENT_ADDRESS) ?: ""
            destinationType = it.getStringExtra(ARG_PAYMENT_DESTINATION_TYPE) ?: ""
            name = it.getStringExtra(ARG_PAYMENT_NAME) ?: ""
            mobile = it.getStringExtra(ARG_PAYMENT_MOBILE) ?: ""
            accountType = it.getStringExtra(ARG_ACCOUNT_TYPE) ?: ""
            blindPayFlow = it.getBooleanExtra(ARG_BLIND_PAY_FLOW, false)
            profileName = it.getStringExtra(ARG_PROFILE_NAME) ?: ""
            kycStatus = it.getStringExtra(ARG_KYC_STATUS) ?: ""
            kycRiskCategory = it.getStringExtra(ARG_KYC_RISK_CATEGORY) ?: ""
            futureAmountLimit = it.getLongExtra(ARG_FUTURE_AMOUNT_LIMIT, 0L)
            supportType = it.getStringExtra(ARG_SUPPORT_TYPE) ?: ""
            destinationUpdateAllowed = it.getBooleanExtra(ARG_DESTINATION_UPDATE_ALLOWED, false)
        }
    }

    private fun setUpNavigation() {
        navHostFragment = findFragmentById(R.id.navHostFragment) as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.payment_navigation_flow)
        val navController = navHostFragment.navController
        when (intent.extras?.getInt(PAYMENT_START_SCREEN, PAYMENT_JUSPAY_SCREEN)) {
            PAYMENT_JUSPAY_SCREEN -> navGraph.startDestination = R.id.juspay_payment_bottom_sheet
            PAYMENT_BLIND_PAY_FLOW -> navGraph.startDestination = R.id.payment_blind_pay_flow
        }
        navController.setGraph(navGraph, intent.extras)
    }

    override fun onJuspaySdkOpened() {
        // this check will help to not to exit activity when we are dismissing editAmountBottomSheet intentionally
        onDismissActivityShouldExit = false
        navHostFragment.navController.popBackStack()
    }

    override fun onJuspaySdkClosed(
        paymentId: String,
        paymentType: String,
        amount: Long,
        errorType: JuspayErrorType,
    ) {
        ongoingPaymentAmount = amount
        if (errorType == JuspayErrorType.NONE) {
            gotoPaymentResultScreen(paymentId, paymentType, false)
        } else {
            when (errorType) {
                JuspayErrorType.JP_001 -> {
                    gotoErrorScreen(PaymentErrorType.OTHER.value)
                }
                JuspayErrorType.JP_002 -> {
                    gotoPaymentResultScreen(paymentId, paymentType, true)
                }
                JuspayErrorType.JP_005 -> {
                    gotoErrorScreen(PaymentErrorType.NETWORK.value)
                }
                else -> {
                }
            }
        }
    }

    override fun onJuspaySdkBackpressed() {
        actionBeforeDismiss()
        finish()
    }

    override fun onApiFailure(errorType: ApiErrorType) {
        // this check will help to not to exit activity when we are dismissing editAmountBottomSheet intentionally
        // its first time error page on click of proceed button
        onDismissActivityShouldExit = false
        when (errorType) {
            ApiErrorType.NETWORK -> {
                gotoErrorScreen(PaymentErrorType.NETWORK.value)
            }
            ApiErrorType.OTHER -> {
                gotoErrorScreen(PaymentErrorType.OTHER.value)
            }
            ApiErrorType.AUTH -> shortToast(R.string.payment_other_error)
            ApiErrorType.NONE -> {
            }
        }
    }

    private fun initJuspaySdk() {
        paymentNavigator.get().initiateJuspayWorkerFragment(this)
    }

    override fun onBackPressed() {
        if (paymentNavigator.get().isBackPressed(this)) {
            return
        }
        super.onBackPressed()
        if (!navHostFragment.navController.popBackStack()) {
            actionBeforeDismiss()
            finish()
        }
    }

    override fun onEditDetailsClicked() {
        editDestinationListener?.onEditDestinationClicked(supplierId)
        finish()
    }

    override fun onDismissEditAmountSheet() {
        if (onDismissActivityShouldExit) {
            finish()
        }
    }

    private fun actionBeforeDismiss() {
        if (supportType.isNotBlank()) editDestinationListener?.onExitFromPaymentFlow("juspay_webview")
    }

    private fun gotoPaymentResultScreen(paymentId: String, paymentType: String, showTxnCancelled: Boolean = false) {
        var displayName = name
        if (blindPayFlow) {
            displayName = profileName
        }

        val action =
            PaymentNavigationFlowDirections.actionToSupplierPaymentResult(
                supplierId,
                paymentId,
                paymentType,
                riskType,
                paymentAddress,
                destinationType,
                displayName,
                mobile,
                accountType,
                blindPayFlow,
            )
        action.txnCancelled = showTxnCancelled

        navHostFragment.navigate(action)
    }

    override fun onRetryClicked() {
        openJuspaySdk()
        val action =
            PaymentNavigationFlowDirections.actionToLoaderScreen()

        navHostFragment.navigate(action)
    }

    override fun onNetworkError() {
        gotoErrorScreen(PaymentErrorType.NETWORK.value)
    }

    override fun onOtherError() {
        gotoErrorScreen(PaymentErrorType.OTHER.value)
    }

    private fun gotoErrorScreen(errorType: String) {
        val action =
            PaymentNavigationFlowDirections.actionToErrorScreen(supplierId, errorType)

        navHostFragment.navigate(action)
    }

    private fun openJuspaySdk() {
        paymentNavigator.get().startJuspaySdk(
            this,
            linkId,
            ongoingPaymentAmount
        )
    }

    override fun onDestroy() {
        editDestinationListener = null
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
