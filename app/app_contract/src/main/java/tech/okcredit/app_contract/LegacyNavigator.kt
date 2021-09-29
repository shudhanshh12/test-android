package tech.okcredit.app_contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import tech.okcredit.camera_contract.CapturedImage

@Deprecated("Use navigation component instead")
interface LegacyNavigator {
    fun gotoWelcomeLanguageSelectionScreen(context: Context)
    fun gotoWelcomeSocialValidationScreen(context: Context)
    fun goToEnterMobileScreen(context: Context, mobileNumber: String = "")
    fun goToOtpScreen(context: Context, mobile: String, flag: Int, isGooglePopupSelected: Boolean = false)
    fun goToSyncScreen(context: Context, skipSelectBusinessScreen: Boolean = false)

    fun goToHome(activity: Activity)

    fun gotoCustomerProfile(context: Activity, customerId: String, isEditMobile: Boolean = false)
    fun goToLoginScreenForAuthFailure(context: Context)
    fun goToDeleteTxnScreenForResult(context: Activity, transactionId: String, requestCode: Int)
    fun goToDeleteSupplierTxnScreen(context: Context, transactionId: String)
    fun goToDeleteCustomerScreen(context: Context, customerId: String)
    fun goToForgotPasswordScreen(context: Context, mobile: String)
    fun gotoPrivacyScreen(context: Context)
    fun goToTransactionDetailFragment(context: Context, txId: String)

    fun goToCustomerScreen(context: Context, customerId: String)
    fun goToAddTransactionScreen(context: Context, customerId: String, isFromAddTransactionShortcut: Boolean = false)
    fun goToMerchantProfileForSetupProfile(context: Context)
    fun goToMerchantProfileForBusinessCardShare(context: Context)

    fun goToMerchantProfileAndShowProfileImage(context: Context)
    fun goToMerchantPageAndAskPermission(context: Context)
    fun goToMerchantInputScreen(
        context: Context,
        inputType: Int,
        inputTitle: String,
        inputValue: String? = null,
        selectedCategoryId: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        gps: Boolean = false,
        isSourceInAppNotification: Boolean = false,
    )

    fun goToMerchantInputScreenForResult(
        fragment: Fragment,
        inputType: Int,
        inputTitle: String,
        requestCode: Int,
        inputValue: String? = null,
        selectedCategoryId: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        gps: Boolean = false,
        isSourceInAppNotification: Boolean = false,
    )

    fun goToMerchantProfile(context: Context)
    fun goToAccountScreen(context: Context)
    fun goWebExperimentScreen(context: Context, type: String, queryParams: Map<String, String>? = null)
    fun goToWebViewScreen(activity: Activity, url: String)
    fun goToAccountStatementScreen(context: Context, source: String)

    fun goToAboutScreen(context: Context)

    fun goToWhatsAppScreen(context: Context)
    fun goToManualChatScreen(context: Context)

    fun goToSupplierScreen(activity: Context, supplierId: String)
    fun goToSupplierPaymentScreen(context: Context, supplierId: String)
    fun startingSupplierIntent(activity: Context, supplierId: String)
    fun startingSupplierScreenForReactivation(activity: Context, supplierId: String, supplierName: String?)
    fun startingCustomerScreenForReactivation(activity: Context, customerId: String, customerName: String?)
    fun goToSupplierTransactionScreen(context: Context, txId: String)
    fun gotoSupplierProfile(context: Activity, customerId: String, isEditMobile: Boolean = false)
    fun gotoSupplierProfileForAddingMobile(context: Context, customerId: String)

    fun goToMerchantDestinationScreen(context: Context)

    fun goToRewardsScreenByClearingBackStack(activity: Activity)

    fun goToOtpVerification(context: Context, requestCode: Int)

    fun goToMerchantDestinationScreenByClearingBackStack(activity: Activity)

    fun goToCollectionTutorialScreen(
        context: Context,
        source: String?,
        rewardAmount: Long? = null,
        redirectToRewardsPage: Boolean? = false,
        action: String? = null,
    )

    fun goToCollectionTutorialScreenByClearingBackStack(
        activity: Activity,
        source: String?,
        rewardAmount: Long? = null,
        redirectToRewardsPage: Boolean? = false,
    )

    fun goToDueCustomerScreenByClearingBackStack(
        activity: Activity,
        sourceScreen: String,
        rewardAmount: Long? = null,
        redirectToRewardsPage: Boolean? = false,
    )

    fun goToSingleListCustomerDestinationScreen(context: Context, requestCode: Int, customerId: String)

    fun goToChangeNumberScreen(context: Context)
    fun goToCameraActivity(
        context: Context,
        requestCode: Int,
        flow: String?,
        relation: String?,
        type: String?,
        screen: String?,
        account: String?,
        mobile: String?,
        existingImages: Int,
    )

    fun goToPlayStore(activity: Activity)

    fun goToMultipleImageSelectedScreen(
        context: Context,
        multipleImageRequestCode: Int,
        imageList: CapturedImage,
        receiptUrl: List<CapturedImage>,
        flow: String?,
        relation: String?,
        type: String?,
        screen: String?,
        account: String?,
        mobile: String?,
        txnId: String? = null,
    )

    fun goToCustomerScreen(context: FragmentActivity, customerId: String, source: String?)
    fun goToAppLockScreen(context: Context)
    fun goToKnowMoreScreen(context: Context, id: String, accountType: String)

    fun goToSystemAppLockScreen(context: Context, source: String)
    fun goToSystemAppLockScreenOnAppResume(context: Context)
    fun goToSystemAppLockScreenFromLogin(context: Context)
    fun goToOnboardBusinessNameScreen(context: Context)
    fun goToHelpV2Screen(context: Context, helpId: List<String>, source: String)
    fun goToHelpHomeScreen(context: Context, helpId: List<String>, source: String)

    fun goToDeeplinkScreen(context: Context, deepLink: String)
    fun goToMoveToSupplierScreen(context: Context, customerId: String)
    fun gotToAddDiscountScreen(context: Context, customerId: String?)
    fun gotoDiscountDetailsScreen(context: Context, txnId: String)

    fun goToFeedbackScreen(context: Context)

    fun goToCollectionScreen(context: Context, toScreen: Int, source: String)

    fun getNumberChangeScreenIntent(): Intent
    fun getWelcomeLanguageScreenIntent(): Intent
    fun getCategoryScreenIntent(): Intent
    fun goToSalesOnCashScreen(context: Context)
    fun goToPhoneNumberChangeConfirmationScreen(activity: Activity, number: String, requestCode: Int)
    fun goToCustomLockScreen(fragment: Fragment, requestCode: Int)
    fun goToLoginScreen(activity: Activity)

    fun goToLanguageScreen(activity: Activity)
    fun goToResetPasswordScreen(activity: Activity, mobile: String, screen: String)
    fun goToPasswordEnableScreen(activity: Activity)

    fun goToQRScannerScreen(fragment: Fragment, requestCode: Int)
}
