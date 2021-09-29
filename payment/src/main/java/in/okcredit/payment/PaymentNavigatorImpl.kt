package `in`.okcredit.payment

import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_BALANCE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_BLIND_PAY_FLOW
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_DESTINATION_UPDATE_ALLOWED
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_FUTURE_AMOUNT_LIMIT
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_KYC_RISK_CATEGORY
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_KYC_STATUS
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_MAX_DAILY_LIMIT_EDT_PAGE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_MERCHANT_ID_EDT_PAGE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_ACCOUNT_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_DESTINATION_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_LINK_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_MOBILE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_NAME
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_PAYMENT_ADDRESS
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_PROFILE_IMAGE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_RISK_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PROFILE_NAME
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_REMAINING_DAILY_LIMIT_EDT_PAGE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_SUPPORT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.PAYMENT_BLIND_PAY_FLOW
import `in`.okcredit.payment.PaymentActivity.Companion.PAYMENT_JUSPAY_SCREEN
import `in`.okcredit.payment.PaymentActivity.Companion.PAYMENT_START_SCREEN
import `in`.okcredit.payment.PaymentActivity.Companion.newInstance
import `in`.okcredit.payment.contract.AddPaymentDestinationListener
import `in`.okcredit.payment.contract.BlindPayListener
import `in`.okcredit.payment.contract.EditDestinationListener
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationDialog
import `in`.okcredit.payment.ui.blindpay.BlindPayDialog
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerFragment
import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import javax.inject.Inject

class PaymentNavigatorImpl @Inject constructor() : PaymentNavigator {
    override fun initiateJuspayWorkerFragment(
        activity: FragmentActivity,

    ) {
        JuspayWorkerFragment.initiateJuspayWorkerFragment(activity)
    }

    override fun isBackPressed(activity: FragmentActivity): Boolean {
        return JuspayWorkerFragment.isBackPressed(activity)
    }

    override fun startJuspaySdk(activity: FragmentActivity, linkId: String, amount: Long) {
        JuspayWorkerFragment.startJuspaySdk(activity, linkId, amount)
    }

    override fun gotoJuspayPaymentEditAmountScreen(
        context: Activity,
        supplierId: String,
        maxDailyLimit: Long,
        remainingDailyAmount: Long,
        supplierBalance: Long,
        merchantId: String,
        riskType: String,
        linkId: String,
        mobile: String,
        paymentAddress: String,
        destinationType: String,
        name: String,
        accountType: String,
        kycStatus: String,
        kycRiskCategory: String,
        futureAmountLimit: Long,
        listener: EditDestinationListener,
        isBlindPayFlow: Boolean,
        profileImage: String,
        profileName: String,
        supportType: String,
        destinationUpdateAllowed: Boolean,
    ) {
        context.startActivity(
            newInstance(context, listener).apply {
                if (isBlindPayFlow) {
                    putExtra(PAYMENT_START_SCREEN, PAYMENT_BLIND_PAY_FLOW)
                } else {
                    putExtra(PAYMENT_START_SCREEN, PAYMENT_JUSPAY_SCREEN)
                }
                putExtra(ARG_PAYMENT_ACCOUNT_ID, supplierId)
                putExtra(ARG_MAX_DAILY_LIMIT_EDT_PAGE, maxDailyLimit)
                putExtra(ARG_REMAINING_DAILY_LIMIT_EDT_PAGE, remainingDailyAmount)
                putExtra(ARG_ACCOUNT_BALANCE, supplierBalance)
                putExtra(ARG_MERCHANT_ID_EDT_PAGE, merchantId)
                putExtra(ARG_PAYMENT_RISK_TYPE, riskType)
                putExtra(ARG_PAYMENT_LINK_ID, linkId)
                putExtra(ARG_PAYMENT_MOBILE, mobile)
                putExtra(ARG_PAYMENT_PAYMENT_ADDRESS, paymentAddress)
                putExtra(ARG_PAYMENT_DESTINATION_TYPE, destinationType)
                putExtra(ARG_PAYMENT_NAME, name)
                putExtra(ARG_ACCOUNT_TYPE, accountType)
                putExtra(ARG_BLIND_PAY_FLOW, isBlindPayFlow)
                putExtra(ARG_PAYMENT_PROFILE_IMAGE, profileImage)
                putExtra(ARG_PROFILE_NAME, profileName)
                putExtra(ARG_KYC_STATUS, kycStatus)
                putExtra(ARG_KYC_RISK_CATEGORY, kycRiskCategory)
                putExtra(ARG_FUTURE_AMOUNT_LIMIT, futureAmountLimit)
                putExtra(ARG_SUPPORT_TYPE, supportType)
                putExtra(ARG_DESTINATION_UPDATE_ALLOWED, destinationUpdateAllowed)
            }
        )
    }

    override fun gotoAddPaymentDestinationDialog(
        accountId: String,
        accountType: String,
        mobile: String,
        name: String,
        dueBalance: Long,
        profileImage: String,
        childFragmentManager: FragmentManager,
        listener: AddPaymentDestinationListener,
        isBlindPayEnabled: Boolean,
    ) {
        val paymentDialogFrag = AddPaymentDestinationDialog.newInstance(
            accountId, accountType, mobile, name, dueBalance, profileImage, isBlindPayEnabled
        )
        paymentDialogFrag.setListener(listener)
        paymentDialogFrag.show(childFragmentManager, AddPaymentDestinationDialog.TAG)
    }

    override fun startPspProfileManagementSdk(
        activity: FragmentActivity,
    ) {
        JuspayWorkerFragment.startPspProfileManagementSdk(activity)
    }

    override fun startPspIncomingIntentSdk(
        activity: FragmentActivity,
        intentData: String,
    ) {
        JuspayWorkerFragment.startPspIncomingIntentSdk(activity, intentData)
    }

    override fun startApproveCollectRequestSdk(
        activity: FragmentActivity,
        gatewayTxnId: String,
        gatewayRefId: String,
    ) {
        JuspayWorkerFragment.startApproveCollectRequestSdk(activity, gatewayTxnId, gatewayRefId)
    }

    override fun startPspUpiActivity(
        activity: FragmentActivity,
    ) {
        activity.startActivity(Intent(activity, PspUpiActivity::class.java))
    }

    override fun gotoBlindPayDialog(
        listener: BlindPayListener,
        childFragmentManager: FragmentManager,
        ledgerType: String,
        accountId: String,
    ) {
        val blindPayDialog = BlindPayDialog.newInstance(ledgerType, accountId)
        blindPayDialog.setListener(listener)
        blindPayDialog.show(childFragmentManager, BlindPayDialog.TAG)
    }
}
