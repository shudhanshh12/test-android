package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class GetCashSalesTest {

    private val salesRepository: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSales = GetCashSales(salesRepository, { getActiveBusinessId })
    private val dateTime: DateTime = mock()

    private val businessId = "business-id"

    @Test
    fun `get sales list`() {
        val testResponse = Models.SalesListResponse(
            salesList = listOf(),
            totalAmount = 0.0,
            totalNumberOfSales = 0.0
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(salesRepository.getSales(null, null, businessId)).thenReturn(Single.just(testResponse))
        val testObserver = getSales.execute(GetCashSales.Request(businessId)).test()
        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).getSales(null, null, businessId)
    }

    @Test
    fun `get sales list with startTime`() {
        val testResponse = Models.SalesListResponse(listOf(), 0.0, 0.0)
        whenever(salesRepository.getSales(null, null, businessId)).thenReturn(
            Single.just
            (testResponse)
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val testObserver = getSales.execute(GetCashSales.Request(businessId, startDate = dateTime)).test()
        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).getSales(null, null, businessId)
    }

    @Test
    fun `get sales list with endTime`() {
        val testResponse = Models.SalesListResponse(listOf(), 0.0, 0.0)
        whenever(salesRepository.getSales(null, null, businessId)).thenReturn(
            Single.just
            (testResponse)
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val testObserver = getSales.execute(GetCashSales.Request(businessId, endDate = dateTime)).test()
        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).getSales(null, null, businessId)
    }

    @Test
    fun `get sales list with range`() {
        val testResponse = Models.SalesListResponse(listOf(), 0.0, 0.0, dateTime, dateTime)
        whenever(salesRepository.getSales(dateTime.millis, dateTime.millis, businessId)).thenReturn(
            Single.just
            (testResponse)
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        val testObserver = getSales.execute(GetCashSales.Request(businessId, dateTime, dateTime)).test()
        testObserver.assertValues(Result.Progress(), Result.Success(testResponse))
        verify(salesRepository).getSales(dateTime.millis, dateTime.millis, businessId)
    }
}
