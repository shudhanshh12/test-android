package tech.okcredit.home.ui.homesearch

object HomeConstants {

    enum class HomeTab {
        CUSTOMER_TAB,
        SUPPLIER_TAB
    }

    /****************** Sort ******************/
    const val SORT_TYPE_NAME = 0
    const val SORT_TYPE_ABS_BALANCE = 1
    const val SORT_TYPE_RECENT_ACTIVITY = 2
    const val SORT_TYPE_LAST_PAYMENT = 3
    const val SORT_TYPE_PAYMENT_DUE_CROSSED = 4
    const val SORT_PAYMENT_DUE_TODAY = 5
    const val SORT_FILTER_DUE_TODAY = "due_today"
    const val SORT_FILTER_DUE_CROSSED = "due_crossed"
    const val SORT_FILTER_UPCOMING_DUE = "upcoming_due"

    const val SORT_BY_NAME = "name"
    const val SORT_BY_LATEST = "latest"
    const val SORT_BY_AMOUNT = "amount"

    /****************** App Sync DateTime ******************/
    const val APP_SYNC_DATE_TIME = "home.KEY_APP_SYNC_DATE_TIME"

    /****************** Supplier Tutorial ******************/
    const val GO_TO_SUPPLIER_TAB = 555

    /****************** User added ******************/
    const val USER_ADDED = 100 // can be customer / supplier

    /****************** set new pin/ update pin *********/
    const val SET_NEW_SECURITY_PIN = 17001
    const val UPDATE_SECURITY_PIN = 17002
}

object Sort {
    fun reset() {
        sortfilter.clear()
        sortBy = HomeConstants.SORT_BY_LATEST
        sortApplied = false
    }

    var isDefaultSortByApplied = true
    const val sortByDefault: String = HomeConstants.SORT_BY_LATEST
    var sortfilter = mutableListOf<String>()
    var sortBy: String = HomeConstants.SORT_BY_LATEST
    var sortApplied = false
}
