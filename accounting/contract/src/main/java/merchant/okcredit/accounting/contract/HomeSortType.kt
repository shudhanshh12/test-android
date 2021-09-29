package merchant.okcredit.accounting.contract

enum class HomeSortType(val value: Int) {
    NAME(0),
    AMOUNT(1),
    ACTIVITY(2),
    NONE(3);

    companion object {
        val map = values().associateBy(HomeSortType::value)

        fun fromValue(value: Int) = map[value] ?: NONE
    }
}
