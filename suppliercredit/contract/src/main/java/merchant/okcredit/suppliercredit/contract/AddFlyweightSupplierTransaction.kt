package merchant.okcredit.suppliercredit.contract

import org.joda.time.DateTime

interface AddFlyweightSupplierTransaction {

    suspend fun execute(
        businessId: String,
        transactionId: String,
        isPayment: Boolean,
        supplierId: String,
        amount: Long,
        note: String?,
        billDate: DateTime,
    )
}
