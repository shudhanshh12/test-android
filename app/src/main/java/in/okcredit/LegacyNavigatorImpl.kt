package `in`.okcredit

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Constants
import `in`.okcredit.collection_ui.ui.home.CollectionsHomeActivity
import `in`.okcredit.collection_ui.ui.qr_scanner.QrScannerActivity
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.MainActivity.Companion.startCustomerIntent
import `in`.okcredit.frontend.ui.MainActivity.Companion.startNumberChangeScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntent
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForAccountStatementScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForAuthFailure
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForCategoryScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForChangeNumberScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForDiscountDetailsScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForDiscountScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForDueCustomerScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForEnterMobileScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForEnterOtpScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForFeedBackScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForHelpHomeScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForHelpV2Screen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForKnowMore
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForMerchantScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForMoveToSupplierScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForOnboardingBusinessName
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForOtpVerificationScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForPhoneNumberChangeConfirmationScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForRewardsScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForSingleListCustomerDestinationScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForSupplierTransactionDetailsScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForSyncScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForTransactionDetailsFragment
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForWelcomeLanguageSelectionScreen
import `in`.okcredit.frontend.ui.MainActivity.Companion.startingIntentForWelcomeSocialValidationScreen
import `in`.okcredit.frontend.ui.MainActivityTranslucentFullScreen
import `in`.okcredit.frontend.ui.SupplierActivity
import `in`.okcredit.frontend.ui.SupplierActivity.Companion.startingIntentForSupplierPaymentScreen
import `in`.okcredit.frontend.ui.SupplierActivity.Companion.startingIntentForSupplierScreen
import `in`.okcredit.frontend.ui.applock.AppLockFragment
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Source.ADD_TRANSACTION_SHORTCUT_SCREEN
import `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.AddTxnContainerActivity.Source.CUSTOMER_SCREEN
import `in`.okcredit.merchant.profile.BusinessFragment
import `in`.okcredit.navigation.NavigationActivity
import `in`.okcredit.notification.DeepLinkActivity
import `in`.okcredit.onboarding.otp_verification.OtpArgs.FLAG_AUTH_FAIL
import `in`.okcredit.sales_ui.SalesActivity
import `in`.okcredit.ui.app_lock.prompt.AppLockPromptActivity
import `in`.okcredit.ui.app_lock.set.AppLockActivity
import `in`.okcredit.ui.customer_profile.CustomerProfileActivity
import `in`.okcredit.ui.delete_customer.DeleteCustomerActivity
import `in`.okcredit.ui.delete_txn.DeleteTransactionActivity
import `in`.okcredit.ui.delete_txn.supplier.transaction.DeleteSupplierTransactionActivity
import `in`.okcredit.ui.language.InAppLanguageActivity
import `in`.okcredit.ui.reset_pwd.ResetPwdActivity
import `in`.okcredit.ui.supplier_profile.SupplierProfileActivity
import `in`.okcredit.ui.whatsapp.WhatsAppActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.camera.CameraActivity.Companion.createIntent
import com.camera.selected_image.MultipleImageActivity
import dagger.Lazy
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.home.ui.acccountV2.ui.AccountActivity.Companion.startingIntent
import tech.okcredit.web.WebUrlNavigator
import tech.okcredit.web.ui.WebViewActivity.Companion.startingIntentForExperiment
import zendesk.chat.ChatConfiguration
import zendesk.chat.ChatEngine
import zendesk.chat.PreChatFormFieldStatus
import zendesk.messaging.MessagingActivity
import java.util.*
import javax.inject.Inject

class LegacyNavigatorImpl @Inject constructor(
    private val context: Lazy<Context>,
    private val tracker: Lazy<Tracker>,
    private val webUrlNavigator: Lazy<WebUrlNavigator>,
) : LegacyNavigator {

    companion object {
        private const val ARG_SOURCE = "Source"
    }

    override fun gotoCustomerProfile(context: Activity, customerId: String, isEditMobile: Boolean) {
        context.startActivityForResult(
            CustomerProfileActivity.startingIntent(context, customerId, isEditMobile),
            MainActivity.CUSTOMER_PROFILE_ACTIVITY_RESULT_CODE
        )
    }

    override fun goToLoginScreenForAuthFailure(context: Context) {
        context.startActivity(startingIntentForAuthFailure(context, FLAG_AUTH_FAIL))
        (context as Activity).finishAffinity()
    }

    override fun goToForgotPasswordScreen(context: Context, mobile: String) {
        context.startActivity(ResetPwdActivity.startingIntent(context, mobile, ResetPwdActivity.REQUESTED_SCREEN_TX))
    }

    override fun goToDeleteTxnScreenForResult(context: Activity, transactionId: String, requestCode: Int) {
        context.startActivityForResult(DeleteTransactionActivity.startingIntent(context, transactionId), requestCode)
    }

    override fun goToDeleteSupplierTxnScreen(context: Context, transactionId: String) {
        context.startActivity(DeleteSupplierTransactionActivity.startingIntent(context, transactionId))
    }

    override fun goToDeleteCustomerScreen(context: Context, customerId: String) {
        context.startActivity(DeleteCustomerActivity.startingIntent(context, customerId))
    }

    override fun gotoPrivacyScreen(context: Context) {
        context.startActivity(startingIntent(context, "", MainActivity.PRIVACY_SCREEN))
    }

    override fun goToTransactionDetailFragment(context: Context, txId: String) {
        context.startActivity(startingIntentForTransactionDetailsFragment(context, txId))
    }

    override fun goToHome(activity: Activity) {
        NavigationActivity.navigateToHomeScreen(activity)
    }

    override fun goToOtpScreen(context: Context, mobile: String, flag: Int, isGooglePopupSelected: Boolean) {
        context.startActivity(startingIntentForEnterOtpScreen(context, mobile, flag, false, isGooglePopupSelected))
    }

    override fun goToCustomerScreen(context: Context, customerId: String) {
        context.startActivity(startingIntent(context, customerId, MainActivity.CUSTOMER_SCREEN))
    }

    override fun goToAddTransactionScreen(
        context: Context,
        customerId: String,
        isFromAddTransactionShortcut: Boolean
    ) {
        val intent: Intent = AddTxnContainerActivity.getAddTransactionIntent(
            context = context,
            customerId = customerId,
            source = if (isFromAddTransactionShortcut) ADD_TRANSACTION_SHORTCUT_SCREEN else CUSTOMER_SCREEN
        )
        intent.putExtra(MainActivity.ARG_SOURCE, ARG_SOURCE)
        context.startActivity(intent)
    }

    override fun goToMerchantProfileForSetupProfile(context: Context) {
        context.startActivity(startingIntentForMerchantScreen(context, true, false))
    }

    override fun goToMerchantProfileForBusinessCardShare(context: Context) {
        context.startActivity(startingIntentForMerchantScreen(context, false, true))
    }

    override fun goToMerchantProfileAndShowProfileImage(context: Context) {
        val intent = startingIntentForMerchantScreen(context, false, false)
        intent.putExtra(BusinessFragment.ARG_SHOW_MERCHANT_PROFILE, true)
        context.startActivity(intent)
    }

    override fun goToMerchantPageAndAskPermission(context: Context) {
        val intent = startingIntentForMerchantScreen(context, false, false)
        intent.putExtra(BusinessFragment.ARG_SHOW_MERCHANT_LOCATION, true)
        context.startActivity(intent)
    }

    override fun goToMerchantInputScreen(
        context: Context,
        inputType: Int,
        inputTitle: String,
        inputValue: String?,
        selectedCategoryId: String,
        latitude: Double,
        longitude: Double,
        gps: Boolean,
        isSourceInAppNotification: Boolean
    ) {
        (context as Activity).startActivity(
            MainActivityTranslucentFullScreen.startingIntentForMerchantScreenInput(
                context,
                inputType,
                inputTitle,
                inputValue,
                selectedCategoryId,
                latitude,
                longitude,
                gps,
                isSourceInAppNotification
            )
        )
        context.overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
    }

    override fun goToMerchantInputScreenForResult(
        fragment: Fragment,
        inputType: Int,
        inputTitle: String,
        requestCode: Int,
        inputValue: String?,
        selectedCategoryId: String,
        latitude: Double,
        longitude: Double,
        gps: Boolean,
        isSourceInAppNotification: Boolean
    ) {
        val intent: Intent = MainActivityTranslucentFullScreen.startingIntentForMerchantScreenInput(
            fragment.requireContext(),
            inputType,
            inputTitle,
            inputValue,
            selectedCategoryId,
            latitude,
            longitude,
            gps,
            isSourceInAppNotification
        )
        fragment.startActivityForResult(intent, requestCode)
        (fragment.requireActivity() as Activity).overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
    }

    override fun goToMerchantProfile(context: Context) {
        context.startActivity(startingIntentForMerchantScreen(context, setupProfile = false, shareBusinessCard = false))
    }

    override fun goToAccountScreen(context: Context) {
        context.startActivity(startingIntent(context))
    }

    override fun goWebExperimentScreen(context: Context, type: String, queryParams: Map<String, String>?) {
        context.startActivity(startingIntentForExperiment(context, type, queryParams))
    }

    override fun goToWebViewScreen(activity: Activity, url: String) {
        webUrlNavigator.get().openUrl(activity, url)
    }

    override fun goToAccountStatementScreen(context: Context, source: String) {
        context.startActivity(startingIntentForAccountStatementScreen(context, source))
    }

    override fun goToAboutScreen(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(Constants.ABOUT_URL)
        context.startActivity(intent)
    }

    override fun goToWhatsAppScreen(context: Context) {
        context.startActivity(WhatsAppActivity.startingIntent(context, true))
    }

    override fun goToSupplierScreen(context: Context, supplierId: String) {
        context.startActivity(startingIntentForSupplierScreen(context, supplierId))
    }

    override fun goToSupplierPaymentScreen(context: Context, supplierId: String) {
        context.startActivity(startingIntentForSupplierPaymentScreen(context, supplierId, true))
    }

    override fun startingSupplierIntent(context: Context, supplierId: String) {
        context.startActivity(SupplierActivity.startingSupplierIntent(context, supplierId))
    }

    override fun startingSupplierScreenForReactivation(context: Context, supplierId: String, supplierName: String?) {
        context.startActivity(
            SupplierActivity.startingSupplierScreenForReactivation(
                context,
                supplierId,
                supplierName
            )
        )
    }

    override fun startingCustomerScreenForReactivation(context: Context, customerId: String, customerName: String?) {
        context.startActivity(MainActivity.startingCustomerScreenForReactivation(context, customerId, customerName))
    }

    override fun goToSupplierTransactionScreen(context: Context, txId: String) {
        context.startActivity(startingIntentForSupplierTransactionDetailsScreen(context, txId))
    }

    override fun gotoSupplierProfile(context: Activity, suppplierId: String, isEditMobile: Boolean) {
        context.startActivityForResult(
            SupplierProfileActivity.startingIntent(context, suppplierId, isEditMobile),
            MainActivity.SUPPLIER_PROFILE_ACTIVITY_RESULT_CODE
        )
    }

    override fun gotoSupplierProfileForAddingMobile(context: Context, customerId: String) {
        context.startActivity(SupplierProfileActivity.startingIntent(context, customerId, true))
    }

    override fun goToMerchantDestinationScreen(context: Context) {
        context.startActivity(CollectionsHomeActivity.getIntent(context))
    }

    override fun goToRewardsScreenByClearingBackStack(activity: Activity) {
        activity.startActivities(
            arrayOf(
                NavigationActivity.homeScreenIntent(activity),
                startingIntentForRewardsScreen(activity)
            )
        )
        activity.finishAffinity()
    }

    override fun goToOtpVerification(context: Context, requestCode: Int) {
        (context as Activity).startActivityForResult(
            startingIntentForOtpVerificationScreen(context, requestCode),
            requestCode
        )
    }

    override fun goToMerchantDestinationScreenByClearingBackStack(activity: Activity) {
        activity.startActivities(
            arrayOf(
                NavigationActivity.homeScreenIntent(activity),
                CollectionsHomeActivity.getIntent(activity)
            )
        )
        activity.finishAffinity()
    }

    override fun goToCollectionTutorialScreen(
        context: Context,
        source: String?,
        rewardAmount: Long?,
        redirectToRewardsPage: Boolean?,
        action: String?
    ) {

        val intent = CollectionsHomeActivity.getIntent(context)
        if (!TextUtils.isEmpty(source)) {
            intent.putExtra(MainActivity.ARG_SOURCE, source)
        }
        if (!TextUtils.isEmpty(action)) {
            intent.putExtra(MainActivity.ARG_ACTION, action)
        }
        intent.putExtra(MainActivity.ARG_REWARDS_AMOUNT, rewardAmount)
        intent.putExtra(MainActivity.ARG_REDIRECT_TO_REWARDS_PAGE, redirectToRewardsPage)
        context.startActivity(intent)
    }

    override fun goToCollectionTutorialScreenByClearingBackStack(
        activity: Activity,
        source: String?,
        rewardAmount: Long?,
        redirectToRewardsPage: Boolean?
    ) {
        val intent = CollectionsHomeActivity.getIntent(activity)
        if (!TextUtils.isEmpty(source)) {
            intent.putExtra(MainActivity.ARG_SOURCE, source)
        }
        intent.putExtra(MainActivity.ARG_REWARDS_AMOUNT, rewardAmount)
        intent.putExtra(MainActivity.ARG_REDIRECT_TO_REWARDS_PAGE, redirectToRewardsPage)
        activity.startActivities(
            arrayOf(
                NavigationActivity.homeScreenIntent(activity),
                intent
            )
        )
        activity.finishAffinity()
    }

    override fun goToDueCustomerScreenByClearingBackStack(
        activity: Activity,
        source: String,
        rewardAmount: Long?,
        redirectToRewardsPage: Boolean?
    ) {
        tracker.get().trackViewCollectionBulkReminder(source)
        val intent = startingIntentForDueCustomerScreen(activity)
        if (!TextUtils.isEmpty(source)) {
            intent.putExtra(MainActivity.ARG_SOURCE, source)
        }
        intent.putExtra(MainActivity.ARG_REWARDS_AMOUNT, rewardAmount)
        intent.putExtra(MainActivity.ARG_REDIRECT_TO_REWARDS_PAGE, redirectToRewardsPage)
        activity.startActivities(
            arrayOf(
                NavigationActivity.homeScreenIntent(activity),
                CollectionsHomeActivity.getIntent(activity),
                intent
            )
        )
        activity.finishAffinity()
    }

    override fun goToChangeNumberScreen(context: Context) {
        context.startActivity(startingIntentForChangeNumberScreen(context))
    }

    override fun goToCameraActivity(
        context: Context,
        requestCode: Int,
        flow: String?,
        relation: String?,
        type: String?,
        screen: String?,
        account: String?,
        mobile: String?,
        existingImages: Int
    ) {
        val activity = context as Activity
        activity.startActivityForResult(
            createIntent(
                context,
                flow!!,
                relation!!,
                type!!,
                screen!!,
                account,
                mobile, existingImages
            ),
            requestCode
        )
    }

    override fun goToMultipleImageSelectedScreen(
        context: Context,
        multipleImageRequestCode: Int,
        selectedImage: CapturedImage,
        transactionImageList: List<CapturedImage>,
        flow: String?,
        relation: String?,
        type: String?,
        screen: String?,
        account: String?,
        mobile: String?,
        txnId: String?
    ) {
        val activity = context as Activity
        activity.startActivityForResult(
            MultipleImageActivity.createSelectedImagesIntent(
                context, selectedImage,
                transactionImageList as ArrayList<CapturedImage>, flow, relation, type, screen, account, mobile, txnId
            ),
            multipleImageRequestCode
        )
    }

    override fun goToSyncScreen(context: Context, skipSelectBusinessScreen: Boolean) {
        context.startActivity(startingIntentForSyncScreen(context, skipSelectBusinessScreen))
    }

    override fun gotoWelcomeLanguageSelectionScreen(context: Context) {
        context.startActivity(startingIntentForWelcomeLanguageSelectionScreen(context))
    }

    override fun gotoWelcomeSocialValidationScreen(context: Context) {
        context.startActivity(startingIntentForWelcomeSocialValidationScreen(context))
    }

    override fun goToCustomerScreen(context: FragmentActivity, customerId: String, source: String?) {
        context.startActivity(startCustomerIntent(context, customerId, MainActivity.CUSTOMER_SCREEN, source!!))
    }

    override fun goToSystemAppLockScreenFromLogin(context: Context) {
        goToSystemAppLockScreen(context, AppLockFragment.LOCK_SETUP_LOGIN_FLOW)
    }

    override fun goToAppLockScreen(context: Context) {
        context.startActivity(AppLockActivity.startingIntent(context))
    }

    override fun goToSystemAppLockScreen(context: Context, source: String) {
        context.startActivity(MainActivityTranslucentFullScreen.startingIntentForAppLockScreen(context, source))
        (context as Activity).overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
    }

    override fun goToSystemAppLockScreenOnAppResume(context: Context) {
        goToSystemAppLockScreen(context, AppLockFragment.AUTHENTICATE_APP_RESUME_SESSION)
    }

    override fun goToKnowMoreScreen(context: Context, id: String, accountType: String) {
        context.startActivity(startingIntentForKnowMore(context, id, accountType))
    }

    override fun goToOnboardBusinessNameScreen(context: Context) {
        context.startActivity(startingIntentForOnboardingBusinessName(context))
    }

    override fun goToHelpV2Screen(context: Context, helpIds: List<String>, source: String) {
        context.startActivity(startingIntentForHelpV2Screen(context, helpIds, source))
    }

    override fun goToDeeplinkScreen(context: Context, deepLink: String) {
        val intent = Intent(context, DeepLinkActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(deepLink)
        context.startActivity(intent)
    }

    override fun goToMoveToSupplierScreen(context: Context, customerId: String) {
        context.startActivity(startingIntentForMoveToSupplierScreen(context, customerId))
    }

    override fun gotToAddDiscountScreen(context: Context, customerId: String?) {
        context.startActivity(startingIntentForDiscountScreen(context, customerId!!))
    }

    override fun gotoDiscountDetailsScreen(context: Context, txnId: String) {
        context.startActivity(startingIntentForDiscountDetailsScreen(context, txnId))
    }

    override fun getNumberChangeScreenIntent() = startNumberChangeScreen(context.get())

    override fun getWelcomeLanguageScreenIntent() = startingIntentForWelcomeLanguageSelectionScreen(context.get())

    override fun getCategoryScreenIntent() = startingIntentForCategoryScreen(context.get())

    override fun goToSalesOnCashScreen(context: Context) {
        context.startActivity(SalesActivity.Companion.getSalesScreenIntent(context))
    }

    override fun goToManualChatScreen(context: Context) {
        val chatConfiguration = ChatConfiguration.builder()
            .withNameFieldStatus(PreChatFormFieldStatus.OPTIONAL)
            .withEmailFieldStatus(PreChatFormFieldStatus.HIDDEN)
            .build()
        MessagingActivity.builder()
            .withEngines(ChatEngine.engine())
            .withToolbarTitle(context.getString(R.string.manual_chat))
            .withBotLabelString(context.getString(R.string.application_name))
            .show(context, chatConfiguration)
    }

    override fun goToEnterMobileScreen(context: Context, mobileNumber: String) {
        context.startActivity(startingIntentForEnterMobileScreen(context, "LOGIN_FLOW", mobileNumber))
    }

    override fun goToPhoneNumberChangeConfirmationScreen(
        activity: Activity,
        number: String,
        requestCode: Int
    ) {
        activity.startActivityForResult(
            startingIntentForPhoneNumberChangeConfirmationScreen(
                activity,
                number
            ),
            requestCode
        )
    }

    override fun goToCustomLockScreen(fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(AppLockPromptActivity.startingIntent(fragment.context), requestCode)
    }

    override fun goToLoginScreen(activity: Activity) {
        gotoWelcomeLanguageSelectionScreen(activity)
    }

    override fun goToPlayStore(activity: Activity) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=in.okcredit.merchant")
            )
        )
    }

    override fun goToFeedbackScreen(context: Context) {
        context.startActivity(startingIntentForFeedBackScreen(context))
    }

    override fun goToSingleListCustomerDestinationScreen(
        context: Context,
        requestCode: Int,
        customerId: String
    ) {
        (context as Activity).startActivityForResult(
            startingIntentForSingleListCustomerDestinationScreen(
                context,
                requestCode, customerId
            ),
            requestCode
        )
    }

    override fun goToQRScannerScreen(fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(Intent(fragment.requireContext(), QrScannerActivity::class.java), requestCode)
    }

    override fun goToCollectionScreen(context: Context, toScreen: Int, source: String) {
        val intent = CollectionsHomeActivity.getIntent(context)
        intent.putExtra(MainActivity.ARG_SOURCE, source)
        context.startActivity(intent)
    }

    override fun goToLanguageScreen(activity: Activity) {
        activity.startActivity(InAppLanguageActivity.startingIntent(activity))
    }

    override fun goToResetPasswordScreen(activity: Activity, mobile: String, screen: String) {
        activity.startActivity(
            ResetPwdActivity.startingIntent(
                activity,
                mobile,
                ResetPwdActivity.REQUESTED_SCREEN_SECURITY
            )
        )
    }

    override fun goToPasswordEnableScreen(activity: Activity) {
        activity.startActivity(startingIntent(activity, "", MainActivity.PASSWORD_ENABLE_SCREEN))
    }

    override fun goToHelpHomeScreen(context: Context, helpId: List<String>, source: String) {
        context.startActivity(startingIntentForHelpHomeScreen(context, helpId, source))
    } // STOP ADDING ANY MORE METHODS HERE. IT'S DEPRECATED
}
