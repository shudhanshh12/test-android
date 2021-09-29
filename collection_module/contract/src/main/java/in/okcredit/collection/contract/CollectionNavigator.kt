package `in`.okcredit.collection.contract

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

interface CollectionNavigator {

    fun showSetUpCollectionDialog(
        fragmentManager: FragmentManager,
        amount: Long,
        listener: SetUpCollectionDialogListener,
    )

    fun showAddMerchantDestinationDialog(
        fragmentManager: FragmentManager,
        source: String,
        isUpdateCollection: Boolean = false,
        paymentMethodType: String? = null,
        asyncRequest: Boolean = false,
    )

    fun showCustomerOnlineCollectionDialog(
        fragmentManager: FragmentManager,
        canSetupLater: Boolean = true,
        title: String? = null,
        description: String? = null,
        listener: CustomerOnlineEducationListener?,
    )

    fun showMerchantDestinationDialog(
        fragmentManager: FragmentManager,
        isUpdateCollection: Boolean = false,
        paymentMethodType: String? = null,
        source: String,
        asyncRequest: Boolean = false,
    )

    fun showKycDialog(
        fragmentManager: FragmentManager,
        listener: KycDialogListener,
        kycDialogMode: KycDialogMode,
        kycStatus: KycStatus? = null,
        kycRiskCategory: KycRiskCategory? = null,
        shouldShowCreditCardInfoForKyc: Boolean = false,
    )

    fun goToOnlinePaymentsList(context: Context)

    fun qrCodeIntent(context: Context): Intent

    fun showPaymentReceivedDialog(fragmentManager: FragmentManager, message: String, customerId: String?)

    fun collectionBenefitsActivity(
        context: Context,
        source: String,
        sendReminder: Boolean = false,
        customerId: String? = null, // only needed when send reminder is true
    ): Intent

    fun goToReferralEducationScreen(context: Context, customerId: String?)

    fun goToTargetedReferralInviteScreen(context: Context, customerId: String?)

    fun billingHomeIntent(context: Context): Intent

    // this is done to avoid adding collection_ui dependency to home module
    fun paymentsContainerFragment(): Fragment
}
