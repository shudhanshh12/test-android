package `in`.okcredit.merchant.contract

object BusinessEvents {

    object Key {
        const val KEY_SOURCE = "source"
        const val KEY_TARGET_BUSINESS_ID = "target_business_id"
        const val KEY_SCREEN = "Screen"
        const val KEY_TYPE = "Type"
        const val KEY_FLOW = "Flow"
        const val KEY_flow = "flow"
    }

    object Value {
        const val LEFT_DRAWER = "left_drawer"
        const val MERCHANT = "Merchant"
        const val SCREEN = "select_business_page"
        const val POST_PROFILE_SECTION = "profile_section"
        const val POST_LOGIN_FLOW = "postlogin_flow"
        const val CREATE_NEW_BUSINESS_FLOW = "create_new_business_flow"
    }

    const val SELECT_BUSINESS_PAGE_VIEWED = "select_business_page_viewed"
    const val BUSINESS_SELECTED = "business_selected"
    const val AUTO_SWITCHED_BUSINESS = "auto_switched_business"
    const val CREATE_BUSINESS_STARTED = "create_new_business_started"
    const val NAME_ENTERED = "Name Entered"
    const val NAME_ENTERED_SUCCESSFUL = "name_entered_successful"
    const val VIEW_PROFILE = "View profile"
    const val CREATE_BUSINESS_ERROR = "create_business_error"
    const val SWITCH_BUSINESS_ERROR = "switch_business_error"
}
