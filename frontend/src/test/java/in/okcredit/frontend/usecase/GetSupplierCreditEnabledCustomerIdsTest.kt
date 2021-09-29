package `in`.okcredit.frontend.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS

class GetSupplierCreditEnabledCustomerIdsTest {
    private val keyValService: KeyValService = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"
    private lateinit var getSupplierCreditEnabledCustomerIds: GetSupplierCreditEnabledCustomerIds

    @Before
    fun setUp() {
        getSupplierCreditEnabledCustomerIds =
            GetSupplierCreditEnabledCustomerIds(keyValService, { getActiveBusinessId })
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun kycEnabledCustomer() {
        // given
        whenever(keyValService.contains(eq(PREF_BUSINESS_SC_ENABLED_CUSTOMERS), any())).thenReturn(Single.just(true))
        whenever(keyValService.get(eq(PREF_BUSINESS_SC_ENABLED_CUSTOMERS), any())).thenReturn(Observable.just("true"))

        // when
        val result = getSupplierCreditEnabledCustomerIds.execute(Unit).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success("true")
        )
    }

    @Test
    fun `when kycEnabledCustomer return false`() {
        // given
        whenever(keyValService.contains(eq(PREF_BUSINESS_SC_ENABLED_CUSTOMERS), any())).thenReturn(Single.just(false))

        // when
        val result = getSupplierCreditEnabledCustomerIds.execute(Unit).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success("")
        )
    }

    @Test
    fun `kycEnabledCustomer enabledCustomers return false`() {
        // given
        whenever(keyValService.contains(eq(PREF_BUSINESS_SC_ENABLED_CUSTOMERS), any())).thenReturn(Single.just(true))
        whenever(keyValService.get(eq(PREF_BUSINESS_SC_ENABLED_CUSTOMERS), any())).thenReturn(Observable.just("false"))

        // when
        val result = getSupplierCreditEnabledCustomerIds.execute(Unit).test()

        // then
        result.assertValues(
            Result.Progress(),
            Result.Success("false")
        )
    }
}
