package `in`.okcredit.collection_ui.navigation

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection_ui.dialogs.CustomerOnlineCollectionDialog
import `in`.okcredit.collection_ui.dialogs.SuccessDialog
import `in`.okcredit.collection_ui.ui.benefits.CollectionBenefitsActivity
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivity
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.home_menu.HomePaymentsContainerFragment
import `in`.okcredit.collection_ui.ui.inventory.InventoryActivity
import `in`.okcredit.collection_ui.ui.kyc.KycDialog
import `in`.okcredit.collection_ui.ui.passbook.PassbookActivity
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity.Companion.REFERRAL_EDUCATION_SCREEN
import `in`.okcredit.collection_ui.ui.referral.TargetedReferralActivity.Companion.REFERRAL_INVITE_LIST
import `in`.okcredit.collection_ui.ui.set_up_collections.SetUpCollectionDialog
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import javax.inject.Inject

class CollectionNavigatorImpl @Inject constructor() : CollectionNavigator {

    override fun showSetUpCollectionDialog(
        fragmentManager: FragmentManager,
        amount: Long,
        listener: SetUpCollectionDialogListener,
    ) {
        val dialog = SetUpCollectionDialog.newInstance(amount)
        dialog.initialize(listener)
        dialog.show(fragmentManager, SetUpCollectionDialog.TAG)
    }

    override fun showCustomerOnlineCollectionDialog(
        fragmentManager: FragmentManager,
        canSetupLater: Boolean,
        title: String?,
        description: String?,
        listener: CustomerOnlineEducationListener?,
    ) {
        val dialog = CustomerOnlineCollectionDialog.newInstance().apply {
            if (listener != null) {
                initialise(listener)
            }
        }
        dialog.show(fragmentManager, CustomerOnlineCollectionDialog.TAG)
    }

    override fun showAddMerchantDestinationDialog(
        fragmentManager: FragmentManager,
        source: String,
        isUpdateCollection: Boolean,
        paymentMethodType: String?,
        asyncRequest: Boolean,
    ) {
        val addMerchantDestinationDialog = AddMerchantDestinationDialog.newInstance(
            isUpdateCollection = isUpdateCollection,
            paymentMethodType = paymentMethodType,
            asyncRequest = asyncRequest,
            source = source
        )
        addMerchantDestinationDialog.show(fragmentManager, AddMerchantDestinationDialog.TAG)
    }

    override fun showMerchantDestinationDialog(
        fragmentManager: FragmentManager,
        isUpdateCollection: Boolean,
        paymentMethodType: String?,
        source: String,
        asyncRequest: Boolean,
    ) {
        AddMerchantDestinationDialog.newInstance(isUpdateCollection, paymentMethodType, asyncRequest, source)
            .show(fragmentManager, AddMerchantDestinationDialog.TAG)
    }

    override fun showKycDialog(
        fragmentManager: FragmentManager,
        listener: KycDialogListener,
        kycDialogMode: KycDialogMode,
        kycStatus: KycStatus?,
        kycRiskCategory: KycRiskCategory?,
        shouldShowCreditCardInfoForKyc: Boolean,
    ) {
        val dialog = KycDialog.newInstance(kycDialogMode, kycStatus, kycRiskCategory, shouldShowCreditCardInfoForKyc)
        dialog.setListener(listener)
        dialog.show(fragmentManager, KycDialog.TAG)
    }

    override fun goToOnlinePaymentsList(context: Context) {
        context.startActivity(Intent(context, PassbookActivity::class.java))
    }

    override fun qrCodeIntent(context: Context): Intent {
        return CollectionsHomeActivity.getIntent(context)
    }

    override fun showPaymentReceivedDialog(fragmentManager: FragmentManager, message: String, customerId: String?) {
        val dialog = SuccessDialog.newInstance(msg = message, customerId = customerId)
        dialog.show(fragmentManager, SuccessDialog::class.simpleName)
    }

    override fun collectionBenefitsActivity(
        context: Context,
        source: String,
        sendReminder: Boolean,
        customerId: String?,
    ): Intent {
        return CollectionBenefitsActivity.getIntent(
            context = context,
            source = source,
            sendReminder = sendReminder,
            customerId = customerId
        )
    }

    override fun goToReferralEducationScreen(context: Context, customerId: String?) {
        context.startActivity(
            TargetedReferralActivity.getIntent(
                context,
                REFERRAL_EDUCATION_SCREEN,
                customerId,
            )
        )
    }

    override fun goToTargetedReferralInviteScreen(context: Context, customerId: String?) {
        context.startActivity(
            TargetedReferralActivity.getIntent(
                context,
                REFERRAL_INVITE_LIST,
                customerId,
            )
        )
    }

    override fun billingHomeIntent(context: Context): Intent {
        return `in`.okcredit.collection_ui.ui.inventory.InventoryActivity.getIntent(context)
    }

    override fun paymentsContainerFragment(): Fragment {
        return HomePaymentsContainerFragment()
    }
}
