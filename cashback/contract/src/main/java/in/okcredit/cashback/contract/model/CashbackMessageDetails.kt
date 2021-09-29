package `in`.okcredit.cashback.contract.model

data class CashbackMessageDetails(
    val isFirstTransaction: Boolean,
    val cashbackAmount: Int,
    val minimumPaymentAmount: Int
)
