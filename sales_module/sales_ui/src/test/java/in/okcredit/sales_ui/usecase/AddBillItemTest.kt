package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single

class AddBillItemTest {

    private val salesRepository: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val addBillItem = AddBillItem(salesRepository, { getActiveBusinessId })

    @org.junit.Test
    fun `add bill items`() {
        val testAddBillItem = BillModel.AddBillItem("test", 1.0)
        val testBillItem = BillModel.BillItem("bill_id", "test", 1.0)
        val testRequest = BillModel.AddBillItemRequest(testAddBillItem)
        val testResponse = BillModel.BillItemResponse(testBillItem)
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        whenever(salesRepository.addBillItem(testRequest, businessId)).thenReturn(Single.just(testResponse))

        val testObserver = addBillItem.execute(AddBillItem.Request(businessId, testRequest)).test()

        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))

        verify(salesRepository).addBillItem(testRequest, businessId)
    }
}
