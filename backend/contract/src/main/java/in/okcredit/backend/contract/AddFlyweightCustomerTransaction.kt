package `in`.okcredit.backend.contract

import org.joda.time.DateTime

interface AddFlyweightCustomerTransaction {

    suspend fun execute(
        businessId: String,
        transactionId: String, // Dictated by upstream so that draft id can be passed on
        isPayment: Boolean,
        customerId: String,
        amount: Long,
        note: String?,
        billDate: DateTime,
    )
}
