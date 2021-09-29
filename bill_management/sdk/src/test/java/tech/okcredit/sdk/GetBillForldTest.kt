package tech.okcredit.sdk

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.database.LocalBill
import tech.okcredit.sdk.usecase.GetBillForId

class GetBillForldTest {
    private val billLocalSource: BillLocalSource = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getBillForld = GetBillForId({ billLocalSource }, { getActiveBusinessId })

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val localBill = LocalBill(
            id = "localBillId",
            transactionId = "transactionId",
            accountId = "AccountId",
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
    fun testExecuteFunReturnLocalBill() {
        // given
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(billLocalSource.getBill("billId", businessId)).thenReturn(Observable.just(localBill))
        // when
        val result = getBillForld.execute("billId").test()

        // then
        result.assertValue {
            it.equals(localBill)
        }
    }
}
