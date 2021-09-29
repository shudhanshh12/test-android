package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncDirtyTransactions
import `in`.okcredit.backend.contract.AddFlyweightCustomerTransaction
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Transaction.Type
import `in`.okcredit.merchant.core.model.Transaction.Type.CREDIT
import `in`.okcredit.merchant.core.model.Transaction.Type.PAYMENT
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject

class AddFlyweightCustomerTransactionImpl @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val collectionAPI: Lazy<CollectionRepository>,
    private val syncDirtyTransactions: Lazy<SyncDirtyTransactions>,
    private val coreSdk: Lazy<CoreSdk>,
) : AddFlyweightCustomerTransaction {

    override suspend fun execute(
        businessId: String,
        transactionId: String,
        isPayment: Boolean,
        customerId: String,
        amount: Long,
        note: String?,
        billDate: DateTime,
    ) {
        if (amount == 0L) error("Amount cannot be 0")

        val typeInt = if (isPayment) PAYMENT else CREDIT
        val isCoreSdkEnabled = coreSdk.get().isCoreSdkFeatureEnabled(businessId).await()

        if (isCoreSdkEnabled) {
            coreAddTransaction(
                businessId = businessId,
                type = typeInt,
                transactionId = transactionId,
                customerId = customerId,
                amount = amount,
                note = note,
                billDate = billDate,
            )
        } else {
            backendAddTransaction(
                businessId = businessId,
                type = typeInt.code,
                transactionId = transactionId,
                customerId = customerId,
                amount = amount,
                note = note,
                billDate = billDate,
            )
        }
    }

    private suspend fun coreAddTransaction(
        businessId: String,
        type: Type,
        transactionId: String,
        customerId: String,
        amount: Long,
        note: String?,
        billDate: DateTime,
    ) {
        coreSdk.get().processTransactionCommand(
            Command.CreateTransaction(
                customerId = customerId,
                transactionId = transactionId,
                type = type,
                amount = amount,
                note = note,
                billDate = Timestamp(billDate.millis),
            ),
            businessId,
        ).await()
    }

    private suspend fun backendAddTransaction(
        businessId: String,
        type: Int,
        transactionId: String,
        customerId: String,
        amount: Long,
        note: String?,
        billDate: DateTime,
    ) {
        val transaction = createTransaction(
            customerId = customerId,
            transactionId = transactionId,
            type = type,
            amount = amount,
            note = note,
            billDate = billDate,
        )
        transactionRepo.get().putTransaction(transaction, businessId).await()
        syncDirtyTransactions.get().schedule(businessId).await()
        collectionAPI.get().deleteCollectionShareInfoOfCustomer(transaction.customerId).await()
    }

    private fun createTransaction(
        customerId: String,
        transactionId: String,
        type: Int,
        amount: Long,
        note: String?,
        billDate: DateTime,
    ): Transaction {
        val now = DateTimeUtils.currentDateTime()
        return Transaction(
            id = transactionId,
            type = type,
            customerId = customerId,
            collectionId = null,
            amountV2 = amount,
            receiptUrl = null,
            note = note,
            createdAt = now,
            isOnboarding = false,
            isDeleted = false,
            deleteTime = null,
            isDirty = true,
            billDate = billDate,
            updatedAt = now,
            isSmsSent = false,
            isCreatedByCustomer = false,
            isDeletedByCustomer = false,
            inputType = null,
            voiceId = null,
            transactionState = Transaction.CREATED,
            transactionCategory = Transaction.DEAFULT_CATERGORY,
            amountUpdated = false,
            amountUpdatedAt = null
        )
    }
}
