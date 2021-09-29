package `in`.okcredit.backend.contract

/**
 * Created by anjal on 23/07/18.
 */
object DeepLinkUrl {
    const val HOME = Constants.DEEPLINK_BASE_URL + "/home"
    const val HOME_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home"

    const val ACCOUNT = Constants.DEEPLINK_BASE_URL + "/account"
    const val ACCOUNT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account"

    const val BACKUP = Constants.DEEPLINK_BASE_URL + "/account/backup"
    const val BACKUP_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/backup"

    const val ACCOUNT_STATEMENT = Constants.DEEPLINK_BASE_URL + "/account_statement"
    const val ACCOUNT_STATEMENT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account_statement"

    const val SECURITY = Constants.DEEPLINK_BASE_URL + "/account/security"
    const val SECURITY_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/security"

    const val APP_LOCK = Constants.DEEPLINK_BASE_URL + "/account/security/app_lock"
    const val APP_LOCK_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/security/app_lock"

    const val PAYMENT_PASSWORD = Constants.DEEPLINK_BASE_URL + "/account/security/payment_password"
    const val PAYMENT_PASSWORD_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/security/payment_password"

    const val PROFILE = Constants.DEEPLINK_BASE_URL + "/account/profile"
    const val PROFILE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/profile"

    const val BUSINESS_CARD = Constants.DEEPLINK_BASE_URL + "/account/profile/business_card"
    const val BUSINESS_CARD_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/profile/business_card"

    const val BUSINESS_LOCATION = Constants.DEEPLINK_BASE_URL + "/account/profile/location"
    const val BUSINESS_LOCATION_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/profile/location"

    const val BUSINESS_CATEGORY = Constants.DEEPLINK_BASE_URL + "/account/profile/category"
    const val BUSINESS_CATEGORY_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/profile/category"

    const val BUSINESS_TYPE = Constants.DEEPLINK_BASE_URL + "/account/profile/type"
    const val BUSINESS_TYPE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/profile/type"

    const val LANGUAGE = Constants.DEEPLINK_BASE_URL + "/account/language"
    const val LANGUAGE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/language"

    const val SHARE = Constants.DEEPLINK_BASE_URL + "/share"
    const val SHARE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/share"

    const val SHARE_AND_EARN = Constants.DEEPLINK_BASE_URL + "/share_v2"
    const val SHARE_AND_EARN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/share_v2"

    const val PRIVACY = Constants.DEEPLINK_BASE_URL + "/privacy"
    const val PRIVACY_V2 = Constants.DEEPLINK_V2_BASE_URL + "/privacy"

    const val RATING = Constants.DEEPLINK_BASE_URL + "/rating"
    const val RATING_V2 = Constants.DEEPLINK_V2_BASE_URL + "/rating"

    const val WELCOME = Constants.DEEPLINK_BASE_URL + "/welcome"
    const val WELCOME_V2 = Constants.DEEPLINK_V2_BASE_URL + "/welcome"

    const val CUSTOMER = Constants.DEEPLINK_BASE_URL + "/customer/{customer_id}"
    const val CUSTOMER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/customer/{customer_id}"

    const val SUPPLIER = Constants.DEEPLINK_BASE_URL + "/supplier/{supplier_id}"
    const val SUPPLIER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/supplier/{supplier_id}"

    const val LIVESALE_SCREEN = Constants.DEEPLINK_BASE_URL + "/livesales/{customer_id}"
    const val LIVESALE_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/livesales/{customer_id}"

    const val CUSTOMER_EDIT = Constants.DEEPLINK_BASE_URL + "/customer/{customer_id}/edit"
    const val CUSTOMER_EDIT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/customer/{customer_id}/edit"

    const val SUPPLIER_EDIT = Constants.DEEPLINK_BASE_URL + "/supplier/{supplier_id}/edit"
    const val SUPPLIER_EDIT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/supplier/{supplier_id}/edit"

    const val CUSTOMER_DELETE = Constants.DEEPLINK_BASE_URL + "/customer/{customer_id}/delete"
    const val CUSTOMER_DELETE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/customer/{customer_id}/delete"

    const val SUPPLIER_DELETE = Constants.DEEPLINK_BASE_URL + "/supplier/{supplier_id}/delete"
    const val SUPPLIER_DELETE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/supplier/{supplier_id}/delete"

    const val CUSTOMER_STATEMENT = Constants.DEEPLINK_BASE_URL + "/customer/{customer_id}/customer_statement"
    const val CUSTOMER_STATEMENT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/customer/{customer_id}/customer_statement"

    const val SUPPLIER_STATEMENT = Constants.DEEPLINK_BASE_URL + "/supplier/{supplier_id}/supplier_statement"
    const val SUPPLIER_STATEMENT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/supplier/{supplier_id}/supplier_statement"

    const val CUSTOMER_STATEMENT_REMINDER =
        Constants.DEEPLINK_BASE_URL + "/customer/{customer_id}/customer_statement/reminder"
    const val CUSTOMER_STATEMENT_REMINDER_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/customer/{customer_id}/customer_statement/reminder"

    const val HELP_V2 = Constants.DEEPLINK_V2_BASE_URL + "/help"
    const val HELP_SECTION_V2 = Constants.DEEPLINK_V2_BASE_URL + "/help/{section_id}"
    const val HELP_SUB_SECTION_V2 = Constants.DEEPLINK_V2_BASE_URL + "/help/{section_id}/{sub_section_id}"

    const val WHATSAPP_REG_SUCCESS = Constants.DEEPLINK_BASE_URL + "/register/success"
    const val WHATSAPP_REG_SUCCESS_V2 = Constants.DEEPLINK_V2_BASE_URL + "/register/success"

    const val WHATSAPP_REG_ERROR_TRY_AGAIN = Constants.DEEPLINK_BASE_URL + "/register/try_again"
    const val WHATSAPP_REG_ERROR_TRY_AGAIN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/register/try_again"

    const val WHATSAPP_REG_ERROR_MOBILE_MISMATCH = Constants.DEEPLINK_BASE_URL + "/register/mobile_mismatch"
    const val WHATSAPP_REG_ERROR_MOBILE_MISMATCH_V2 = Constants.DEEPLINK_V2_BASE_URL + "/register/mobile_mismatch"

    const val COLLECTION_EDIT_UPI = Constants.DEEPLINK_BASE_URL + "/account/collections/edit_upi"
    const val COLLECTION_EDIT_UPI_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/collections/edit_upi"

    const val COLLECTION_ADOPTION_SCREEN = Constants.DEEPLINK_BASE_URL + "/account/collection/collection_adoption"
    const val COLLECTION_ADOPTION_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/collection/collection_adoption"

    const val MERCHANT_COLLECTION_SCREEN =
        Constants.DEEPLINK_BASE_URL + "/account/collections" + "/merchant_collection_screen"
    const val MERCHANT_COLLECTION_SCREEN_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/account/collections" + "/merchant_collection_screen"

    const val COLLECTION_ADOPTION_SCREEN_CONFIGURABLE =
        Constants.DEEPLINK_BASE_URL + "/account" + "/collection" + "/collection_adoption/{adoption_title}"
    const val COLLECTION_ADOPTION_SCREEN_CONFIGURABLE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/collection" +
        "/collection_adoption/{adoption_title}"

    const val COLLECTION_SCREEN_DETAIL_SCREEN = Constants.DEEPLINK_BASE_URL + "/account/collection/{collections_id}"
    const val COLLECTION_SCREEN_DETAIL_SCREEN_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/account/collection/{collections_id}"

    // TODO Keeping it for backward compatibility remove it in release v2.21.0
    const val UPI_SCREEN = Constants.DEEPLINK_BASE_URL + "/account/collections/upi"
    const val UPI_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/collections/upi"

    const val MERCHANT_DESTINATION_SCREEN = Constants.DEEPLINK_BASE_URL + "/account/collections/merchant_destination"
    const val MERCHANT_DESTINATION_SCREEN_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/account/collections/merchant_destination"

    const val MERCHANT_BULK_REMINDER = Constants.DEEPLINK_BASE_URL + "/account/bulk_reminder"
    const val MERCHANT_BULK_REMINDER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/bulk_reminder"

    const val HOME_FILTER_DUE_TODAY = Constants.DEEPLINK_BASE_URL + "/home/filter_due_today"
    const val DUE_CUSTOMER = Constants.DEEPLINK_BASE_URL + "/due_customer"
    const val DUE_CUSTOMER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/due_customer"

    const val DUE_SUPPLIER = Constants.DEEPLINK_BASE_URL + "/due_supplier"
    const val DUE_SUPPLIER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/due_supplier"

    const val HOME_ADD_CUSTOMER = Constants.DEEPLINK_BASE_URL + "/home/add_customer"
    const val HOME_ADD_CUSTOMER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home/add_customer"

    const val HOME_ONE_TAP_COLLECTION_POPUP = Constants.DEEPLINK_BASE_URL + "/home/collection_popup"
    const val HOME_ONE_TAP_COLLECTION_POPUP_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home/collection_popup"

    const val HOME_IN_APP_REVIEW = Constants.DEEPLINK_BASE_URL + "/home/inapp_review"
    const val HOME_IN_APP_REVIEW_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home/inapp_review"

    const val REWARDS_SCREEN = Constants.DEEPLINK_BASE_URL + "/account/collections/rewards"
    const val REWARDS_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/collections/rewards"

    const val REFERRAL = Constants.DEEPLINK_BASE_URL + "/referral"
    const val REFERRAL_V2 = Constants.DEEPLINK_V2_BASE_URL + "/referral"

    const val CHANGE_LANGUAGE = Constants.DEEPLINK_BASE_URL + "/language"
    const val CHANGE_LANGUAGE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/language"

    const val WEB_EXPERIMENT = Constants.DEEPLINK_BASE_URL + "/experiments/{experiment_name}"
    const val LOAD_WEB_PARAM_URL = "web_url"
    const val LOAD_WEB = Constants.DEEPLINK_BASE_URL + "/web/{" + LOAD_WEB_PARAM_URL + "}"

    const val CATEGORIES = Constants.DEEPLINK_BASE_URL + "/categories"
    const val CATEGORIES_V2 = Constants.DEEPLINK_V2_BASE_URL + "/categories"

    const val HELP_NEW = Constants.DEEPLINK_BASE_URL + "/helpv2"
    const val HELP_NEW_V2 = Constants.DEEPLINK_V2_BASE_URL + "/helpv2"

    const val MANUAL_CHAT = Constants.DEEPLINK_BASE_URL + "/manual_chat"
    const val MANUAL_CHAT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/manual_chat"

    const val HELP_INSTRUCTION = Constants.DEEPLINK_BASE_URL + "/helpv2/{help_item_id}"
    const val HELP_INSTRUCTION_V2 = Constants.DEEPLINK_V2_BASE_URL + "/helpv2/{help_item_id}"

    const val EXPENSE_MANAGER = Constants.DEEPLINK_BASE_URL + "/expense_manager"
    const val EXPENSE_MANAGER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/expense_manager"

    const val CASH_SALES = Constants.DEEPLINK_BASE_URL + "/cashsales"
    const val CASH_SALES_V2 = Constants.DEEPLINK_V2_BASE_URL + "/cashsales"

    const val ADD_EXPENSE = Constants.DEEPLINK_BASE_URL + "/add/expense"
    const val ADD_EXPENSE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/add/expense"

    const val ADD_SALE = Constants.DEEPLINK_BASE_URL + "/add/sale"
    const val ADD_SALE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/add/sale"

    const val LENDING_SME = Constants.DEEPLINK_BASE_URL + "/lending_sme"
    const val LENDING_SME_V2 = Constants.DEEPLINK_V2_BASE_URL + "/lending_sme"

    const val BUSINESS_HEALTH_DASHBOARD = Constants.DEEPLINK_BASE_URL + "/dashboard/health"
    const val BUSINESS_HEALTH_DASHBOARD_V2 = Constants.DEEPLINK_V2_BASE_URL + "/dashboard/health"

    const val ACCOUNT_CHAT =
        Constants.DEEPLINK_BASE_URL + "/chat/{account_id}/{account_role" +
            "}/{message_id}"
    const val ACCOUNT_CHAT_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/chat/{account_id}/{account_role" +
            "}/{message_id}"

    const val BILL_MANAGEMENT =
        Constants.DEEPLINK_BASE_URL + "/bill/{account_id}/{account_role" +
            "}/{account_name}"
    const val BILL_MANAGEMENT_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/bill/{account_id}/{account_role" +
            "}/{account_name}"

    const val COLLECTION_DEFAULTER_LIST = Constants.DEEPLINK_BASE_URL + "/collection_defaulter_list"
    const val COLLECTION_DEFAULTER_LIST_V2 = Constants.DEEPLINK_V2_BASE_URL + "/collection_defaulter_list"

    const val CASH_COUNTER = Constants.DEEPLINK_BASE_URL + "/cash_counter"
    const val CASH_COUNTER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/cash_counter"

    const val SALES_BILL = Constants.DEEPLINK_BASE_URL + "/sales/bill"
    const val SALES_BILL_V2 = Constants.DEEPLINK_V2_BASE_URL + "/sales/bill"

    const val ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN =
        Constants.DEEPLINK_BASE_URL + "/home/add_transaction_shortcut_search"
    const val ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/home/add_transaction_shortcut_search"

    const val ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_FROM_REFERRAL =
        Constants.DEEPLINK_BASE_URL + "/referral/add_transaction_shortcut_search"
    const val ADD_TRANSACTION_SHORTCUT_SEARCH_SCREEN_FROM_REFERRAL_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/referral/add_transaction_shortcut_search"

    const val USER_MIGRATION_UPLOAD_PDF =
        Constants.DEEPLINK_BASE_URL + "/home/user_migration_upload_pdf"
    const val USER_MIGRATION_UPLOAD_PDF_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/home/user_migration_upload_pdf"

    const val QR_CODE_SCREEN = Constants.DEEPLINK_BASE_URL + "/qrCode"
    const val QR_CODE_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/qrCode"

    const val SUPPLIER_ONLINE_PAYMENT = Constants.DEEPLINK_BASE_URL + "/supplier/{supplier_id}/online_payment"
    const val SUPPLIER_ONLINE_PAYMENT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/supplier/{supplier_id}/online_payment"

    const val SAVE_OKCREDIT_CONTACT =
        Constants.DEEPLINK_BASE_URL + "/save_okcredit_contact/{contact_name}/{phone_number}"
    const val SAVE_OKCREDIT_CONTACT_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/save_okcredit_contact/{contact_name}/{phone_number}"

    const val PSP_UPI_APPROVE_COLLECT =
        Constants.DEEPLINK_BASE_URL + "/psp_upi_approve_collect/{gateway_transaction_id}/{gateway_reference_id}"
    const val PSP_UPI_APPROVE_COLLECT_V2 =
        Constants.DEEPLINK_V2_BASE_URL + "/psp_upi_approve_collect/{gateway_transaction_id}/{gateway_reference_id}"

    const val CUSTOMER_ONLINE_PAYMENT = Constants.DEEPLINK_BASE_URL + "/customer/{customer_id}/online_payment"
    const val CUSTOMER_ONLINE_PAYMENT_V2 = Constants.DEEPLINK_V2_BASE_URL + "/customer/{customer_id}/online_payment"

    const val IPL_SCREEN = Constants.DEEPLINK_BASE_URL + "/ipl2021"
    const val IPL_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/ipl2021"
    const val IPL_WEEKLY_DRAW = Constants.DEEPLINK_BASE_URL + "/ipl2021/weekly_draw"
    const val IPL_WEEKLY_DRAW_V2 = Constants.DEEPLINK_V2_BASE_URL + "/ipl2021/weekly_draw"
    const val IPL_LEADER_BOARD = Constants.DEEPLINK_BASE_URL + "/ipl2021/leaderboard"
    const val IPL_LEADER_BOARD_V2 = Constants.DEEPLINK_V2_BASE_URL + "/ipl2021/leaderboard"
    const val IPL_GAME_SCREEN = Constants.DEEPLINK_BASE_URL + "/ipl2021/game/{match_id}"
    const val IPL_GAME_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/ipl2021/game/{match_id}"

    const val REFERRAL_COLLECTION_ADOPTION_SCREEN =
        Constants.DEEPLINK_BASE_URL + "/account/collection/collection_adoption/referral/{merchant_id}"
    const val REFERRAL_COLLECTION_EDUCATION_SCREEN =
        Constants.DEEPLINK_BASE_URL + "/account/collection/referral/education"
    const val REFERRAL_COLLECTION_LIST_SCREEN =
        Constants.DEEPLINK_BASE_URL + "/account/collection/referral/list"

    const val OKPL_VOICE_COLLECTION_SCREEN = Constants.DEEPLINK_BASE_URL + "/booster/voice_collection"
    const val OKPL_VOICE_COLLECTION_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/booster/voice_collection"

    const val STAFF_COLLECTION_LINK_SCREEN = Constants.DEEPLINK_BASE_URL + "/account/collection/staff_link"
    const val STAFF_COLLECTION_LINK_SCREEN_V2 = Constants.DEEPLINK_V2_BASE_URL + "/account/collection/staff_link"

    const val HOME_BULK_ADD_BY_VOICE = Constants.DEEPLINK_BASE_URL + "/home/bulk_add"
    const val HOME_BULK_ADD_BY_VOICE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home/bulk_add"

    const val HOME_BULK_REMINDER = Constants.DEEPLINK_BASE_URL + "/home/bulk_reminder_v2"
    const val HOME_BULK_REMINDER_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home/bulk_reminder_v2"

    const val HOME_CUSTOMER_PROFILE = Constants.DEEPLINK_BASE_URL + "/home/customer_profile/{customer_id}"
    const val HOME_CUSTOMER_PROFILE_V2 = Constants.DEEPLINK_V2_BASE_URL + "/home/customer_profile/{customer_id}"
}
