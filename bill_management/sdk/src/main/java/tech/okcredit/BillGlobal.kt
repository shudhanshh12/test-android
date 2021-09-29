package tech.okcredit

// TODO: Poor implementation. Please update this
@Deprecated("Poor implementation. All the required info should be queried and passed")
object BillGlobalInfo {
    var unseenAccountBills: Int = 0
    var totalAccountBills: Int = 0
    var accountId: String = ""
    var relation: String = ""
    var value: String = FilterRange.ALL
    var dateRange: MutableList<String> = mutableListOf()
}

object FilterRange {
    const val ALL = "All"
    const val DATE_RANGE = "date_range"
    const val THIS_MONTH = "this_month"
    const val LAST_MONTH = "last_month"
    const val LAST_TO_LAST_MONTH = "last_to_last_month"
}

object Constants {
    const val NEW = "NEW"
    const val UPDATED = "UPDATED"
    const val OLD = "OLD"
}
