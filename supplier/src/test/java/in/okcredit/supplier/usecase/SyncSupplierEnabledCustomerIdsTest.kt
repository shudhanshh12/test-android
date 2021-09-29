package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class SyncSupplierEnabledCustomerIdsTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val syncSupplierEnabledCustomerIds = SyncSupplierEnabledCustomerIdsImpl(
        { supplierCreditRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute should complete`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.syncSupplierEnabledCustomerIds(businessId)).thenReturn(Completable.complete())

        val testObserver = syncSupplierEnabledCustomerIds.execute().test()

        testObserver.assertComplete()
    }
}
