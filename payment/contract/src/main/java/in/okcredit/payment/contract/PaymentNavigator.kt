package `in`.okcredit.payment.contract

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

interface PaymentNavigator {
    fun initiateJuspayWorkerFragment(
        activity: FragmentActivity,
    )

    fun isBackPressed(activity: FragmentActivity): Boolean
    fun startJuspaySdk(activity: FragmentActivity, linkId: String, amount: Long)
    fun gotoJuspayPaymentEditAmountScreen(
        context: Activity,
        accountId: String,
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
        isBlindPayFlow: Boolean = false,
        profileImage: String = "",
        profileName: String = "",
        supportType: String = "",
        destinationUpdateAllowed: Boolean,
    )

    fun gotoAddPaymentDestinationDialog(
        accountId: String,
        accountType: String,
        mobile: String,
        name: String,
        dueBalance: Long,
        profileImage: String,
        childFragmentManager: FragmentManager,
        listener: AddPaymentDestinationListener,
        isBlindPayEnabled: Boolean,
    )

    fun startPspProfileManagementSdk(activity: FragmentActivity)

    fun startPspIncomingIntentSdk(
        activity: FragmentActivity,
        intentData: String,
    )

    fun startApproveCollectRequestSdk(
        activity: FragmentActivity,
        gatewayTxnId: String,
        gatewayRefId: String,
    )

    fun startPspUpiActivity(activity: FragmentActivity)

    fun gotoBlindPayDialog(
        listener: BlindPayListener,
        childFragmentManager: FragmentManager,
        ledgerType: String,
        accountId: String,
    )
}
