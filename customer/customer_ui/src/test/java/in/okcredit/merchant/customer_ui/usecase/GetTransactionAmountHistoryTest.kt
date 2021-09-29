package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetTransactionAmountHistoryTest {

    private val coreSdk: CoreSdk = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var getTransactionAmountHistory: GetTransactionAmountHistory

    @Before
    fun setup() {
        getTransactionAmountHistory = GetTransactionAmountHistory(
            coreSdk,
            { getActiveBusinessId }
        )
    }

    @Test
    fun `should return error not non updated txn`() {
        val txnId = "txn_1"
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(coreSdk.getTxnAmountHistory(txnId, businessId))
            .thenReturn(
                Single.just(
                    TransactionAmountHistory(
                        txnId,
                        "120",
                        false,
                        null,
                        0,
                        null,
                        listOf()
                    )
                )
            )

        whenever(coreSdk.getTransaction(txnId, businessId))
            .thenReturn(
                Observable.just(
                    Transaction(
                        id = txnId,
                        type = Transaction.Type.CREDIT,
                        customerId = "cus_id",
                        amount = 120L,
                        createdAt = Timestamp(0),
                        billDate = Timestamp(0),
                        updatedAt = Timestamp(0),
                        isDeleted = false,
                        images = listOf(),
                        amountUpdated = false,
                        note = "",
                        collectionId = ""
                    )
                )
            )

        val testObserver = getTransactionAmountHistory.execute(txnId).test()

        Truth.assertThat(
            testObserver.errors()
                .contains(GetTransactionAmountHistory.TransactionHistoryNotFountException())
        )
    }

    @Test
    fun `should return history for updated txn`() {
        val txnId = "txn_1"
        val businessId = "business-id"
        val txnHistory = TransactionAmountHistory(
            txnId,
            "120",
            false,
            null,
            0,
            null,
            listOf()
        )
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        whenever(coreSdk.getTxnAmountHistory(txnId, businessId))
            .thenReturn(
                Single.just(
                    txnHistory
                )
            )

        whenever(coreSdk.getTransaction(txnId, businessId))
            .thenReturn(
                Observable.just(
                    Transaction(
                        id = txnId,
                        type = Transaction.Type.CREDIT,
                        customerId = "cus_id",
                        amount = 120L,
                        createdAt = Timestamp(0),
                        billDate = Timestamp(0),
                        updatedAt = Timestamp(0),
                        isDeleted = false,
                        images = listOf(),
                        amountUpdated = true,
                        note = "",
                        collectionId = ""
                    )
                )
            )

        val testObserver = getTransactionAmountHistory.execute(txnId).test()

        Truth.assertThat(
            testObserver.values()
                .contains(txnHistory)
        )
    }
}
