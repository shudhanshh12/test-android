package tech.okcredit.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class GetSupplierCreditEnabledCustomerIdsImplTest {

    private val keyValService: KeyValService = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSupplierCreditEnabledCustomerIdsImpl =
        GetSupplierCreditEnabledCustomerIdsImpl({ keyValService }, { getActiveBusinessId })
    private val businessId = "businessId"

    companion object {
        const val KEY_SC_ENABLED_CUSTOMERS = "key_sc_enabled_customer_ids"
        const val KEY = "key value"
    }

    @Before
    fun setup() {
        whenever(getActiveBusinessId.thisOrActiveBusinessId(anyOrNull())).thenReturn(Single.just(businessId))
    }

    @Test
    fun execute() {
        // given
        whenever(keyValService.contains(eq(KEY_SC_ENABLED_CUSTOMERS), any())).thenReturn(Single.just(true))
        whenever(keyValService.get(eq(KEY_SC_ENABLED_CUSTOMERS), any())).thenReturn(Observable.just(KEY))

        // when
        val testObserver =
            getSupplierCreditEnabledCustomerIdsImpl.execute(businessId).subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertValue(KEY)
        testObserver.dispose()
    }

    @Test
    fun `execute when keyvalservice not enabled`() {
        // given
        whenever(keyValService.contains(eq(KEY_SC_ENABLED_CUSTOMERS), any())).thenReturn(Single.just(false))
        whenever(keyValService.get(eq(KEY_SC_ENABLED_CUSTOMERS), any())).thenReturn(Observable.just(""))

        // when
        val testObserver =
            getSupplierCreditEnabledCustomerIdsImpl.execute(businessId).subscribeOn(Schedulers.trampoline()).test()

        // then
        testObserver.assertValue("")
        testObserver.dispose()
    }
}
