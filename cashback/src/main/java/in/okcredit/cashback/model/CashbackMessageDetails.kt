package `in`.okcredit.cashback.model

data class CashbackMessageDetails(
    val isFirstTransaction: Boolean,
    val cashbackAmount: Int,
    val minimumPaymentAmount: Int
)
