package merchant.okcredit.accounting.contract.model

enum class LedgerType(val value: String) {
    CUSTOMER("CUSTOMER"),
    SUPPLIER("SUPPLIER")
}

enum class LedgerTxnStatus(val value: String) {
    FAILED("Failed"),
    PENDING("Pending"),
    REFUND_INITIATED("Refund Initiated"),
}

fun LedgerType.isCustomer() = this == LedgerType.CUSTOMER
fun LedgerType.isSupplier() = this == LedgerType.SUPPLIER
