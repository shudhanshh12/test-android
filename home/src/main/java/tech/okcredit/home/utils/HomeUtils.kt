package tech.okcredit.home.utils

import `in`.okcredit.analytics.PropertyValue
import tech.okcredit.home.ui.homesearch.HomeSearchContract

const val MERCHANT_DESTINATION_SCREEN = 25
const val COLLECTION_ADOPTION_SCREEN = 71
const val REQUESTED_SCREEN_SECURITY = "SECURITY_SCREEN"
const val CUSTOM_APP_LOCK = "CUSTOM_APP_LOCK"
const val SYSTEM_APP_LOCK = "SYSTEM_APP_LOCK"
const val NO_APP_LOCK = "NO_APP_LOCK"
const val LOCK_SETUP_SETTING_SCREEN = "LOCK_SETUP_SETTING_SCREEN"

fun HomeSearchContract.SOURCE.getAnalyticsRelationValue(): String {
    return when (this) {
        HomeSearchContract.SOURCE.HOME_CUSTOMER_TAB -> {
            PropertyValue.CUSTOMER
        }
        HomeSearchContract.SOURCE.HOME_SUPPLIER_TAB -> {
            PropertyValue.SUPPLIER
        }
    }
}
