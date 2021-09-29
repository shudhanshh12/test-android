package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class DeleteSaleTest {

    private val salesRepository: SalesRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val deleteSale = DeleteSale(salesRepository, { getActiveBusinessId })

    @Test
    fun `delete sale`() {
        val saleId = "sale_id"
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(salesRepository.deleteSale(saleId, businessId)).thenReturn(Completable.complete())
        val testObserver = deleteSale.execute(DeleteSale.Request(saleId)).test()

        testObserver.assertValues(Result.Progress(), Result.Success(Unit))
        verify(salesRepository).deleteSale(saleId, businessId)
    }
}
