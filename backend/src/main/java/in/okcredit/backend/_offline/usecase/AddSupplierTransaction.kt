package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.utils.CommonUtils
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime
import tech.okcredit.android.auth.usecases.VerifyPassword
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AddSupplierTransaction @Inject constructor(
    private val supplierCreditRepository: SupplierCreditRepository,
    private val verifyPassword: VerifyPassword,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(req: AddTransactionRequest): Single<Transaction> {

        var authCheck = Completable.complete()
        if (req.isPasswordVerifyRequired) {
            authCheck = verifyPassword.execute(req.password)
        }

        return authCheck
            .andThen(getActiveBusinessId.get().execute())
            .flatMap { businessId ->
                val transaction = transactionWrapper(req)
                supplierCreditRepository.addTransaction(transaction, businessId)
                    .andThen(Single.just(transaction))
            }
            .doOnSuccess { Timber.i(">> addTransaction 3") }
    }

    private fun transactionWrapper(txn: AddTransactionRequest): Transaction {

        return Transaction(
            id = UUID.randomUUID().toString(),
            supplierId = txn.supplierId,
            collectionId = null,
            payment = txn.payment,
            amount = txn.amount,
            note = txn.note,
            receiptUrl = txn.receiptUrl,
            billDate = txn.billDate,
            createTime = CommonUtils.currentDateTime(),
            createdBySupplier = false,
            deleted = false,
            deleteTime = null,
            deletedBySupplier = false,
            updateTime = CommonUtils.currentDateTime(),
            syncing = false,
            lastSyncTime = null,
            transactionState = txn.transactionState
        )
    }

    data class AddTransactionRequest(
        val supplierId: String,
        val amount: Long,
        val payment: Boolean = false,
        val note: String? = null,
        val receiptUrl: String? = null,
        val password: String? = null,
        val billDate: DateTime,
        val isPasswordVerifyRequired: Boolean,
        val transactionState: Int = -1,
    )
}
