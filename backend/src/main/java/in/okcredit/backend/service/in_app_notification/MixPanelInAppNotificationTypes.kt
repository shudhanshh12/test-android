package `in`.okcredit.backend.service.in_app_notification

import java.util.*

object MixPanelInAppNotificationTypes {
    const val TUTORIAL_CALL_CUSTOMER = "TUTORIAL_CALL_CUSTOMER"
    const val TUTORIAL_NOTE_CUSTOMER = "TUTORIAL_NOTE_CUSTOMER"
    const val TUTORIAL_LIVE_SALES = "TUTORIAL_LIVE_SALES"
    const val TUTORIAL_ACCOUNT_STATEMENT = "TUTORIAL_ACCOUNT_STATEMENT"
    const val APP_UPDATE = "APP_UPDATE"
    const val INAPP_RATING_SMILEY = "INAPP_RATING_SMILEY"
    const val INAPP_RATING_STAR = "INAPP_RATING_STAR"
    const val INAPP_REVIEW = "INAPP_REVIEW"
    const val INAPP_SYSTEM_MAINTENANCE = "INAPP_SYSTEM_MAINTENANCE"
    const val INAPP_ADDRESS = "INAPP_ADDRESS"
    const val INAPP_MERCHANT_PROFILE = "INAPP_MERCHANT_PROFILE"
    const val ENABLE_SYSTEM_LOCK_SCREEN = "ENABLE_SYSTEM_LOCK_SCREEN"
    const val FEEDBACK_TECH_ISSUES = "FEEDBACK_TECH_ISSUES"
    const val START_LOGGING_24_HR = "START_LOGGING_24_HR"
    const val START_LOGGING_7_DAYS = "START_LOGGING_7_DAYS"
    const val INAPP_TUTORIAL_REMINDER = "INAPP_TUTORIAL_REMINDER"
    const val SUPPLIER_TAB = "SUPPLIER_TAB"
    const val ADD_SUPPLIER = "ADD_SUPPLIER"
    const val FIRST_SUPPLIER = "FIRST_SUPPLIER"
    const val SUPPLIER_TAKE_GIVE_CREDIT = "SUPPLIER_TAKE_GIVE_CREDIT"
    const val SHOW_INAPP_INSURANCE = "SHOW_INAPP_INSURANCE"
    const val GIVE_DISCOUNT_EDUCATION = "GIVE_DISCOUNT_EDUCATION"
    const val CUSTOMER_MENU_EDUCATION = "CUSTOMER_MENU_EDUCATION"
    const val FIRST_EXPENSE_EDUCATION_1 = "FIRST_EXPENSE_EDUCATION_1"
    const val FIRST_EXPENSE_EDUCATION_2 = "FIRST_EXPENSE_EDUCATION_2"
    const val FIRST_ADD_EXPENSE_EDUCATION = "FIRST_ADD_EXPENSE_EDUCATION"
    const val CALENDAR_PERMISSION = "CALENDAR_PERMISSION"
    const val COLLECTION_DATE_EDUCATION = "COLLECTION_DATE_EDUCATION"
    const val FILTER_EDUCATION_V2 = "FILTER_EDUCATION_V2"
    const val REMIND_EDUCATION = "REMIND_EDUCATION"
    const val PAY_ONLINE_REMINDER_EDUCATION = "PAY_ONLINE_REMINDER_EDUCATION"
    const val SEND_PAYMENT_REMINDER_EDUCATION = "SEND_PAYMENT_REMINDER_EDUCATION"
    const val FIRST_SALE_EDUCATION = "FIRST_SALE_EDUCATION"
    const val SHOW_ONLINE_COLLECTION_POPUP = "SHOW_ONLINE_COLLECTION_POPUP"
    const val SHOW_COLLECTION_ADOPTION_EDUCATION = "SHOW_COLLECTION_ADOPTION_EDUCATION"
    const val INAPP_RECHARGE_SMS = "INAPP_RECHARGE_SMS"
    const val INAPP_MONEY_TRANSFER_SMS = "INAPP_TRANSFER_SMS"
    const val INAPP_MERCHANT_ADDRESS = "INAPP_MERCHANT_ADDRESS"
    const val ADD_TRANSACTION_SHORTCUT = "ADD_TRANSACTION_SHORTCUT"
    const val SHOW_PAY_ONLINE_EDUCATION_HOME = "SHOW_PAY_ONLINE_EDUCATION_HOME"
    const val SHOW_PAY_ONLINE_EDUCATION_FOR_CAMPAIGN = "SHOW_PAY_ONLINE_EDUCATION_FOR_CAMPAIGN"
    const val CHAT_EDUCATION = "CHAT_EDUCATION"
    const val BILL_EDUCATION = "BILL_EDUCATION"
    const val SHOW_REPORT_ICON_EDUCATION = "SHOW_REPORT_ICON_EDUCATION"
    const val SHOW_DATE_SELECTION_EDUCATION_EDUCATION = "SHOW_DATE_SELECTION_EDUCATION_EDUCATION"
    const val SHOW_REPORT_SHARE_EDUCATION = "SHOW_REPORT_SHARE_EDUCATION"
    const val SHOW_EDIT_AMOUNT_EDUCATION = "SHOW_EDIT_AMOUNT_EDUCATION"
    const val SHOW_HOME_DASHBOARD_EDUCATION = "SHOW_HOME_DASHBOARD_EDUCATION"
    const val SHOW_DELETE_TXN_EDUCATION = "SHOW_DELETE_TXN_EDUCATION"
    const val SHOW_REFERRAL_IN_APP_BOTTOMSHEET = "SHOW_REFERRAL_IN_APP_BOTTOMSHEET"
    const val SHOW_ADD_OKCREDIT_CONTACT_BOTTOMSHEET = "SHOW_ADD_OKCREDIT_CONTACT_BOTTOMSHEET"
    const val SET_NEW_PIN = "SET_NEW_PIN"
    const val UPDATE_NEW_PIN = "UPDATE_NEW_PIN"
    const val SHOW_QR_FIRST_EDUCATION = "SHOW_QR_FIRST_EDUCATION"
    const val SHOW_COLLECTION_NUDGE_ON_CUSTOMER_SCREEN = "SHOW_COLLECTION_NUDGE_ON_CUSTOMER_SCREEN"
    const val SHOW_COLLECTION_NUDGE_ON_SET_DUE_DATE = "SHOW_COLLECTION_NUDGE_ON_SET_DUE_DATE"
    const val SHOW_COLLECTION_NUDGE_ON_DUE_DATE_CROSSED = "SHOW_COLLECTION_NUDGE_ON_DUE_DATE_CROSSED"
    const val BULK_REMINDER = "BULK_REMINDER"
    const val CALCULATOR_EDUCATION = "CALCULATOR_EDUCATION"
    const val SHOW_COMPLETE_KYC = "SHOW_COMPLETE_KYC"
    const val SHOW_RISK_KYC = "SHOW_RISK_KYC"
    const val SHOW_KYC_STATUS = "SHOW_KYC_STATUS"
    const val SHOW_KYC_BANNER = "SHOW_KYC_BANNER"
    const val UPLOAD_DB_FILES = "UPLOAD_DB_FILES"

    /**
     * Return all mixpanel inapp events that showed in home screen
     */
    fun homeShownMixPanelInAppNotificationTypes(): List<String> {
        return mutableListOf(
            TUTORIAL_ACCOUNT_STATEMENT,
            APP_UPDATE,
            INAPP_RATING_SMILEY,
            INAPP_RATING_STAR,
            INAPP_SYSTEM_MAINTENANCE,
            ENABLE_SYSTEM_LOCK_SCREEN,
            FEEDBACK_TECH_ISSUES,
            INAPP_TUTORIAL_REMINDER,
            SHOW_INAPP_INSURANCE,
            PAY_ONLINE_REMINDER_EDUCATION,
            SEND_PAYMENT_REMINDER_EDUCATION,
            SHOW_COLLECTION_ADOPTION_EDUCATION,
            INAPP_MERCHANT_ADDRESS,
            ADD_TRANSACTION_SHORTCUT,
            SHOW_PAY_ONLINE_EDUCATION_HOME,
            SHOW_PAY_ONLINE_EDUCATION_FOR_CAMPAIGN,
            BULK_REMINDER,
        )
    }
}