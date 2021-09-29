package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.home.usecase.pre_network_onboarding.HideBigButtonAndNudge

class HideBigButtonAndNudgeTest {
    private val mockCustomerRepo: CustomerRepo = mock()
    private val mockSupplierRepo: SupplierCreditRepository = mock()
    private val mockGetActiveBusinessId: GetActiveBusinessId = mock()

    private val showAddRelationshipCTANudge = HideBigButtonAndNudge(
        { mockCustomerRepo },
        { mockSupplierRepo },
        { mockGetActiveBusinessId }
    )

    @Test
    fun `should return false when both zero customers and zero supplier are added`() {
        val businessId = "businessId"
        whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockCustomerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(0))
        whenever(mockSupplierRepo.getSuppliersCount(businessId)).thenReturn(Observable.just(0))

        val result = showAddRelationshipCTANudge.execute().test()

        result.assertValue(false)
    }

    @Test
    fun `should return true when one or more customers or supplier are added `() {
        val businessId = "businessId"
        whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockCustomerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(1))
        whenever(mockSupplierRepo.getSuppliersCount(businessId)).thenReturn(Observable.just(0))

        val result = showAddRelationshipCTANudge.execute().test()

        result.assertValue(true)
    }

    @Test
    fun `should return true when one or more customers and supplier are added `() {
        val businessId = "businessId"
        whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(mockCustomerRepo.getCustomersCount(businessId)).thenReturn(Observable.just(1))
        whenever(mockSupplierRepo.getSuppliersCount(businessId)).thenReturn(Observable.just(1))

        val result = showAddRelationshipCTANudge.execute().test()

        result.assertValue(true)
    }
}
