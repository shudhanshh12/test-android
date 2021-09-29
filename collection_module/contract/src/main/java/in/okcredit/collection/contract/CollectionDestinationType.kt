package `in`.okcredit.collection.contract

enum class CollectionDestinationType(val value: String) {
    UPI("upi"),
    PAY_TM("paytm"),
    BANK("bank"),
    I_DONT_KNOW("I don't know"),
    NONE("");

    companion object {
        val map = values().associateBy(CollectionDestinationType::value)
        fun fromValue(value: String) = map[value] ?: UPI
    }
}
