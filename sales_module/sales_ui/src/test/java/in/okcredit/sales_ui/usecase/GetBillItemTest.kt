package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class GetBillItemTest {
    private val sales: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getBillItems = GetBillItems(sales, { getActiveBusinessId })

    companion object {
        val billItem = BillModel.BillItem(id = "billId", name = "billName")
        val billItemResponse = BillModel.BillItemListResponse(listOf(billItem))
    }

    @Test
    fun `execute test`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        // given
        whenever(sales.getBillItems(businessId)).thenReturn(Single.just(billItemResponse))

        // when
        val result = getBillItems.execute(GetBillItems.Request(businessId)).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success(billItemResponse)
        )
    }
}
