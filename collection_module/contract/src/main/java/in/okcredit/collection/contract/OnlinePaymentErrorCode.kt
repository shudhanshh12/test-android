package `in`.okcredit.collection.contract

enum class OnlinePaymentErrorCode(val value: String) {
    EP001("EP001"),
    EP002("EP002"),
    EP004("EP004"),
}

enum class PayoutType(val value: String) {
    PAYOUT("Payout"),
    REFUND("Refund"),
}
