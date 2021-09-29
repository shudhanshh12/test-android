package `in`.okcredit.analytics

@Deprecated("Use Event Class Instead")
object AnalyticsEvents {

    /****************************************************************
     * Global
     */
    const val AUTH_FAILURE = "Auth Failure"
    const val LONG_PRESS = "Long Press"

    /****************************************************************
     * Reset Password
     */
    const val RESET_PWD_STARTED = "ResetPassword: Started"
    const val RESET_PWD_OTP_SCREEN = "ResetPassword:OtpScreen"
    const val RESET_PWD_PWD_SCREEN = "ResetPassword:PasswordScreen"
    const val RESET_PWD_INVALID_PASSWORD = "ResetPassword: Inconst valid Password"

    /****************************************************************
     * Settings
     */
    const val IN_APP_LANGUAGE_SCREEN = "InAppLanguageScreen"
    const val LANGUAGE_SELECTED_IN_APP = "LanguageSelectedInApp"

    /****************************************************************
     * Delete Customer
     */
    const val DELETE_CUSTOMER_SCREEN = "DeleteCustomerScreen"
    const val CUSTOMER_PROFILE_SCREEN = "Customer Profile Screen"
    const val DELETE_CUSTOMER_SCREEN_CLICK_DELETE = "DeleteCustomerScreen: Delete Clicked"
    const val DELETE_CUSTOMER_INCORRECT_PASSWORD = "DeleteCustomerScreen: Incorrect Password"
    const val DELETE_CUSTOMER_SCREEN_CLICK_SETTLEMENT = "DeleteCustomerScreen: Settlement Clicked"

    const val DELETE_SUPPLIER_SCREEN = "DeleteSupplierScreen"
    const val SUPPLIER_PROFILE_SCREEN = "Supplier Profile Screen"
    const val DELETE_SUPPLIER_SCREEN_CLICK_DELETE = "DeleteSupplierScreen: Delete Clicked"
    const val DELETE_SUPPLIER_INCORRECT_PASSWORD = "DeleteSupplierScreen: Incorrect Password"
    const val DELETE_SUPPLIER_SCREEN_CLICK_SETTLEMENT = "DeleteSupplierScreen: Settlement Clicked"

    /****************************************************************
     * Reactivate Customer
     */

    const val IN_APP_NOTI_TO_BE_DISPLAYED = "InAppNotification TBD"
    const val IN_APP_NOTI_DISPLAYED = "InAppNotification Displayed"
    const val IN_APP_NOTI_CLICKED = "InAppNotification Clicked"
    const val IN_APP_NOTI_CLEARED = "InAppNotification Cleared"

    const val DELETE_INCORRECT_PASSWORD = "DeleteTxScreen: IncorrectPassword"
    const val TX_DELETE_CONFIRM = "DeleteTxScreen: Confirm"
    const val DELETE_TRANSACTION_SCREEN_DELETE_CLICKED = "DeleteTxScreen: DeleteClicked"
    const val DELETE_TRANSACTION_SUCCESS = "DeleteTxScreen: Success"
    const val DELETE_TRANSACTION_SCREEN = "Delete Transaction Screen"
    const val DELETE_TRANSACTION = "Delete Transaction"

    const val APP_LOCK_SCREEN = "AppLockScreen"
    const val APP_LOCK_SCREEN_SUCCESS = "AppLockScreen: Success"

    const val APP_LOCK_PREF_SCREEN = "AppLockPrefScreen"
    const val APP_LOCK_PROMPT_SCREEN = "AppLockPromptScreen"
    const val FORGOT_APP_LOCK_SCREEN = "ForgotAppLockScreen"
    const val APP_LOCK_WRONG_PATTERN = "AppLockScreen: Wrong Pattern"

    const val PASSWORD_OTP_ENTERED = "ResetPassword: Otp entered"
    const val RESET_PWD_SUCCESSFUL = "ResetPassword: Password Updated"
    const val VIEW_ACCOUNT_STATEMENT = "View Account Statement"
    const val REQUEST_ACCOUNT_STATEMENT = "Request Account Statement"
    const val UPDATE_ACCOUNT_STATEMENT = "Update Account Statement"
    const val DOWNLOAD_ACCOUNT_STATEMENT = "Download Account Statement"
    const val SYNC_TXN = "Sync Transaction"
    const val SHARE_TXN = "Share Transaction"
    const val ADD_TXN_CONFIRM = "Add Transaction: Confirm"
    const val ADD_RECEIPT = "Add Receipt"
    const val DELETE_RECEIPT = "Delete Receipt"
    const val ADD_NOTE = "Add Note"
    const val SELECT_BILL_DATE = "Select Bill Date"
    const val UPDATE_BILL_DATE = "Update Bill Date"
    const val INPUT_CALCULATOR = "Input Calculator"
    const val UPDATE_PASSWORD_SETTING = "Update Settings Password"
    const val PERMISSION_ACCEPT = "Grant Permission"
    const val PERMISSION_DENIED = "Deny Permission"
    const val CONFIRM_PASSWORD = "Confirm Password"
    const val FORGOT_PASSWORD = "Forgot Password"
    const val YOUTUBE_VIDEO = "Youtube Video"
    const val VIEW_ACCOUNT = "View Account"
    const val WHATSAPP_SCREEN_ACTION = "Confirm WhatsAppScreen"
    const val VIEW_SETUP_COLLECTION_DIALOG = "View Setup Collection Dialog"
    const val VIEW_DEEPLINK = "View Deeplink"

    /****************************************************************
     * OnBoarding
     ***************************************************************/

    const val RESEND_OTP = "Resend OTP"
    const val REGISTER_WHATSAPP = "Register WhatsApp"
    const val AUTO_VERIFY_OTP = "Auto Verify Otp"

    /****************************************************************
     * Add Customer
     ***************************************************************/

    const val IMPORT_CONTACT = "Import Contact"
    const val SELECT_CONTACT = "Select Contact"
    const val CONFIRM_NAME = "Confirm Name"
    const val SKIP_MOBILE = "Skip Mobile"
    const val CONFIRM_MOBILE = "Confirm Mobile"
}
