package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class UpdateBillItemTest {

    private val salesRepository: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val updateBillItem = UpdateBillItem(salesRepository, { getActiveBusinessId })

    @Test
    fun `update bill items`() {
        val testRequest = BillModel.UpdateBillItemRequest(
            BillModel.AddBillItem("testName", 2.0),
            listOf("name", "rate")
        )
        val testBillItem = BillModel.BillItem("bill_id", "testName", 2.0)
        val testResponse = BillModel.BillItemResponse(testBillItem)
        val updateBillItemRequest = UpdateBillItem.Request("bill_id", testRequest)

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(salesRepository.updateBillItem("bill_id", testRequest, businessId))
            .thenReturn(Single.just(testResponse))

        val testObserver = updateBillItem.execute(updateBillItemRequest).test()

        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))

        verify(salesRepository).updateBillItem("bill_id", testRequest, businessId)
    }
}
