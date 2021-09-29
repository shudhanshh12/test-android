package tech.okcredit.sdk

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.LocalBill
import tech.okcredit.sdk.usecase.GetTransactionDetails

class GetTransactionDetailsTest {
    private val billLocalSource: BillLocalSource = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getTransactionDetails = GetTransactionDetails({ billLocalSource }, { getActiveBusinessId })

    companion object {
        val request = GetTransactionDetails.Request(billId = "billId")
        val localBill = LocalBill(
            id = "localBillId",
            transactionId = null,
            accountId = null,
            createdByMe = false,
            localBillDocList = listOf(),
            createdAt = "",
            updatedAt = null,
            deletedAt = null,
            note = null,
            amount = null,
            txnName = null,
            billDate = null,
            status = "",
            txnType = null,
            deleted = false
        )
    }

    @Test
    fun returnLocalBillsWhenExecuteCalled() {
        // given
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(billLocalSource.getBill(request.billId, businessId)).thenReturn(Observable.just(localBill))

        // when
        val result = getTransactionDetails.execute(request).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(localBill)
        )
    }
}
