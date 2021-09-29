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

class AddSaleTest {

    private val dateTime: DateTime = mock()
    private val salesRepository: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val addSale = AddSale(salesRepository, { getActiveBusinessId })

    @Test
    fun `add sale with out billing name`() {
        val businessId = "business-id"
        val testRequest = AddSale.Request(businessId, 0.0, "note", dateTime)
        val testAddSale = Models.AddSale(businessId, 0.0, "note", dateTime, null, null)
        val testBillItem = BillModel.BillItem("bill_id", "name", 0.0, 1.0)
        val testSaleItem = Models.SaleItems(listOf())
        val testSale = Models.Sale(0.0, dateTime, null, "sale_id", "note", null, null, null, dateTime, testSaleItem)
        val testResponse = Models.AddSaleResponse(testSale)
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(salesRepository.submitSale(Models.SaleRequestModel(testAddSale), businessId)).thenReturn(
            Single.just(
                testResponse
            )
        )

        val testObservable = addSale.execute(testRequest).test()
        testObservable.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).submitSale(Models.SaleRequestModel(testAddSale), businessId)
    }

    @Test
    fun `add sale with billing name`() {
        val businessId = "business-id"
        val testRequest = AddSale.Request(businessId, 0.0, "note", dateTime, "name", "mobile")
        val testAddSale = Models.AddSale(businessId, 0.0, "note", dateTime, "name", "mobile")
        val testBillItem = BillModel.BillItem("bill_id", "name", 0.0, 1.0)
        val testSaleItem = Models.SaleItems(listOf())
        val testSale =
            Models.Sale(0.0, dateTime, dateTime, "sale_id", "note", "name", "mobile", dateTime, dateTime, testSaleItem)
        val testResponse = Models.AddSaleResponse(testSale)

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(salesRepository.submitSale(Models.SaleRequestModel(testAddSale), businessId)).thenReturn(
            Single.just(
                testResponse
            )
        )

        val testObservable = addSale.execute(testRequest).test()
        testObservable.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).submitSale(Models.SaleRequestModel(testAddSale), businessId)
    }
}
