package `in`.okcredit.collection.contract

object CollectionStatus {
    const val ACTIVE: Int = 1
    const val PAID = 2
    const val EXPIRED = 3
    const val CANCELLED = 4
    const val COMPLETE = 5
    const val FAILED = 6
    const val REFUNDED = 7
    const val REFUND_INITIATED = 8
    const val PAYOUT_FAILED = 9
    const val MIGRATED = 10
    const val PAYOUT_INITIATED = 11
}
