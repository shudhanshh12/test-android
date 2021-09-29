package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class GetCashSaleItemTest {

    private val salesRepository: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSaleItem = GetCashSaleItem(salesRepository, { getActiveBusinessId })

    @Test
    fun `get sale item`() {
        val testBillItem = BillModel.BillItem("bill_id", "name", 0.0, 1.0)
        val testSaleItem = Models.SaleItems(listOf())
        val testSale =
            Models.Sale(
                0.0,
                DateTime.now(),
                null,
                "sale_id",
                "note",
                null,
                null,
                DateTime.now(),
                DateTime.now(),
                testSaleItem
            )
        val testResponse = Models.SaleItemResponse(testSale)
        val testRequest = GetCashSaleItem.Request("sale_id")
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(salesRepository.getSale("sale_id", businessId)).thenReturn(Single.just(testResponse))
        val testObserver = getSaleItem.execute(testRequest).test()
        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).getSale("sale_id", businessId)
    }
}
