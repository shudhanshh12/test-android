package tech.okcredit.base

@Deprecated(message = "Keep constants in respective modules")
object Traces {

    // Customer And Supplier
    const val RENDER_ADD_CUSTOMER = "RenderAddCustomer"
    const val RENDER_ADD_SUPPLIER = "RenderAddSupplier"
    const val RENDER_ADD_SUPPLIER_Transaction = "RenderAddSupplierTransaction"
    const val RENDER_ADD_Transaction = "RenderAddTransaction"
    const val RENDER_CUSTOMER = "RenderCustomer"
    const val RENDER_DUE_CUSTOMER = "RenderDueCustomer"
    const val RENDER_SUPPLIER = "RenderSupplier"
    const val RENDER_SUPPLIER_TRANSACTION = "RenderSupplierTransaction"
    const val RENDER_TRANSACTION_DETAILS = "RenderTransactionDetails"
    const val OnCreateCustomerProfile = "OnCreateCustomerProfile"

    // Merchant
    const val RENDER_MERCHANT_DESTINATION = "RenderMerchantDestination"
    const val RENDER_MERCHANT_PROFILE = "RenderMerchantProfile"
    const val RENDER_NUMBER_CHANGE = "RenderNumberChange"
    const val RENDER_BUSINESS_NAME = "RenderBusinessName"

    // MISC
    const val RENDER_ACCOUNT_STATEMENT = "RenderAccountStatement"
    const val RENDER_KNOW_MORE = "RenderKnowMore"

    /****************************************************************
     * OnBoarding
     */
    const val RENDER_ENTER_OTP = "RenderEnterOtp"
    const val RENDER_CONFIRM_PHONE_CHANGE = "RenderConfirmPhoneChange"
    const val RENDER_ENTER_MOBILE = "RenderEnterMobile"
    const val RENDER_ENTER_NEW_NUMBER = "RenderEnterNewNumber"
    const val RENDER_LANGUAGE = "RenderLanguage"
    const val RENDER_OTP_VERIFICATION = "RenderOTPVerification"
    const val RENDER_ONBOARDING_TUTORIAL = "RenderOnBoardingTutorial"

    /****************************************************************
     * Security
     */
    const val RENDER_APPLOCK = "RenderAppLock"
    const val RENDER_PAYMENT_PASSWORD = "RenderPaymentPassword"
    const val RENDER_PRIVACY = "RenderPrivacy"
    const val OnCreate_AppLock = "OnCreateAppLock"

    /****************************************************************
     * Platform and Global
     */
    const val RENDER_SYNC_SCREEN = "RenderSync"
    const val onCreateCameraActivity = "onCreateCameraActivity"
    const val OnCreate_AppLockPrompt = "OnCreateApplockPrompt"
    const val OnCreate_AppLockPref = "OnCreateAppLockPref"
    const val Trace_syncTxsPerformance = "syncTxsPerformance" // Change Name and Structure of event
    const val Trace_CoreModule_SyncTxsPerformance = "CoreModuleSyncTxsPerformance"
    const val Trace_GetActiveCustomerList = "GetActiveCustomerList" // Change Name and Structure of
    const val Trace_GetActiveCustomerListFromCoreModule = "GetActiveCustomerListFromCoreModule" // event

    const val Trace_BillModule_SyncTxsPerformance = "BillModuleSyncTxsPerformance"
}
