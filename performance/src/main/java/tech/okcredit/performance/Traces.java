package tech.okcredit.performance;

public final class Traces {


    /**
     * Names for custom traces and custom metrics must meet the following requirements:
     * 1. no leading or trailing whitespace
     * 2. no leading underscore
     * 3. max length is 32 characters
     **/


    /****************************************************************
     Account
     ****************************************************************/
    // Customer And Supplier
    public static final String RENDER_ADD_CUSTOMER = "RenderAddCustomer";
    public static final String RENDER_ADD_SUPPLIER = "RenderAddSupplier";
    public static final String RENDER_ADD_SUPPLIER_Transaction = "RenderAddSupplierTransaction";
    public static final String RENDER_ADD_Transaction = "RenderAddTransaction";
    public static final String RENDER_CUSTOMER = "RenderCustomer";
    public static final String RENDER_DUE_CUSTOMER = "RenderDueCustomer";
    public static final String RENDER_SUPPLIER = "RenderSupplier";
    public static final String RENDER_SUPPLIER_TRANSACTION = "RenderSupplierTransaction";
    public static final String RENDER_SUPPLIER_TUTORIAL = "RenderSupplierTutorial";
    public static final String RENDER_TRANSACTION_DETAILS = "RenderTransactionDetails";
    public static final String OnCreateCustomerProfile = "OnCreateCustomerProfile";

    //Merchant
    public static final String RENDER_MERCHANT_DESTINATION = "RenderMerchantDestination";
    public static final String RENDER_MERCHANT_PROFILE = "RenderMerchantProfile";
    public static final String RENDER_NUMBER_CHANGE = "RenderNumberChange";
    public static final String RENDER_BUSINESS_NAME = "RenderBusinessName";


    //MISC
    public static final String OnCreate_Account = "OnCreateAccount";
    public static final String RENDER_ACCOUNT_STATEMENT = "RenderAccountStatement";
    public static final String RENDER_KNOW_MORE = "RenderKnowMore";


    // Rewards
    public static final String RENDER_REWARDS = "RenderRewards";
    public static final String RENDER_FIREWORK_LOTTIE_AIMATION = "RenderFireWorkLottieAnimation";


    /****************************************************************
     OnBoarding
     ****************************************************************/
    public static final String TRACE_LAUNCHER_AUTH = "TracerLauncherAuth";
    public static final String RENDER_ENTER_OTP = "RenderEnterOtp";
    public static final String RENDER_CONFIRM_PHONE_CHANGE = "RenderConfirmPhoneChange";
    public static final String RENDER_ENTER_MOBILE = "RenderEnterMobile";
    public static final String RENDER_ENTER_NEW_NUMBER = "RenderEnterNewNumber";
    public static final String RENDER_LOGIN_SUCCESS = "RenderLoginSuccess";
    public static final String RENDER_LANGUAGE = "RenderLanguage";
    public static final String RENDER_OTP_VERIFICATION = "RenderOTPVerification";
    public static final String RENDER_ONBOARDING_TUTORIAL = "RenderOnBoardingTutorial";


    /****************************************************************
     Security
     ****************************************************************/
    public static final String RENDER_APPLOCK = "RenderAppLock";
    public static final String RENDER_PAYMENT_PASSWORD = "RenderPaymentPassword";
    public static final String RENDER_PRIVACY = "RenderPrivacy";
    public static final String OnCreate_AppLock = "OnCreateAppLock";


    /****************************************************************
     Platform and Global
     ****************************************************************/

    public static final String OnCreate_Application = "OnCreateApplication";
    public static final String RENDER_SYNC_SCREEN = "RenderSync";
    public static final String onCreateCameraActivity = "onCreateCameraActivity";
    public static final String OnCreate_AppLockPrompt = "OnCreateApplockPrompt";
    public static final String OnCreate_AppLockPref = "OnCreateAppLockPref";

}
