package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single

class UpdateTransactionAmountTest {
    private val transactionRepo: TransactionRepo = mock()
    private val coreSdk: CoreSdk = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val updateTransactionAmount = UpdateTransactionAmount(transactionRepo, coreSdk, { getActiveBusinessId })

    // @Test
    fun `excute() return success`() {
        val businessId = "business-id"
        val req = UpdateTransactionAmount.Request(1L, "transaction_id")
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(coreSdk.processTransactionCommand(any(), eq(businessId))).thenReturn(
            Single.just<Transaction>(
                Transaction(
                    "123",
                    Transaction.Type.CREDIT,
                    "1",
                    0L,
                    null,
                    note = "12",
                    createdAt = mock(),
                    billDate = mock(),
                    updatedAt = mock()
                )
            )
        )

        val testObserver = updateTransactionAmount.execute(req).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(
                Unit
            )
        )

        testObserver.dispose()
    }
}
