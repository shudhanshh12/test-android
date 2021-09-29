package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.utils.CommonUtils
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import merchant.okcredit.suppliercredit.contract.AddFlyweightSupplierTransaction
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class AddFlyweightSupplierTransactionImpl @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
) : AddFlyweightSupplierTransaction {

    override suspend fun execute(
        businessId: String,
        transactionId: String, // Dictated by upstream so that draft id can be passed on
        isPayment: Boolean,
        supplierId: String,
        amount: Long,
        note: String?,
        billDate: DateTime,
    ) {
        val now = CommonUtils.currentDateTime()

        val transaction = Transaction(
            id = transactionId,
            supplierId = supplierId,
            collectionId = null,
            payment = isPayment,
            amount = amount,
            note = note,
            receiptUrl = null,
            billDate = billDate,
            createTime = now,
            createdBySupplier = false,
            deleted = false,
            deleteTime = null,
            deletedBySupplier = false,
            updateTime = now,
            syncing = false,
            lastSyncTime = null,
            transactionState = -1
        )

        supplierCreditRepository.get().addTransaction(transaction, businessId).await()
    }
}
