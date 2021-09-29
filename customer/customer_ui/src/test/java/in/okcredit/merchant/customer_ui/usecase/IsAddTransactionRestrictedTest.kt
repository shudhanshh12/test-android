package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.utils.AbFeatures
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS

class IsAddTransactionRestrictedTest {
    private val keyValService: KeyValService = mock()
    private val isSupplierCreditEnabledCustomer: IsSupplierCreditEnabledCustomer = mock()
    private val ab: AbRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var isAddTransactionRestricted: IsAddTransactionRestricted
    private val businessId = "businessId"

    @Before
    fun setUp() {
        isAddTransactionRestricted =
            IsAddTransactionRestricted(keyValService, isSupplierCreditEnabledCustomer, ab, { getActiveBusinessId })

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun whenSingleListFeatureEnabled() {
        // given
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(keyValService.contains(eq(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS), any()))
            .thenReturn(Single.just(true))
        whenever(keyValService[eq(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS), any()])
            .thenReturn(Observable.just("customerId"))

        // when
        val result = isAddTransactionRestricted.execute("customerId").test()

        // then
        result.assertValue { it }
    }

    @Test
    fun `whenSingleListFeatureEnabled but RestrictedCustomer return false`() {
        // given
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(keyValService.contains(eq(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS), any()))
            .thenReturn(Single.just(true))
        whenever(keyValService.get(eq(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS), any()))
            .thenReturn(Observable.just("customerid"))

        // when
        val result = isAddTransactionRestricted.execute("customerId").test()

        // then
        result.assertValue { it.not() }
    }

    @Test
    fun `true whenSingleListFeatureEnabled keyAddTxnRestricted false`() {
        // given
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(keyValService.contains(eq(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS), any()))
            .thenReturn(Single.just(false))

        // when
        val result = isAddTransactionRestricted.execute("customerId").test()

        // then
        result.assertValue { it.not() }
    }

    @Test
    fun `SingleList feature is not enabled`() {
        // given
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(false))
        whenever(isSupplierCreditEnabledCustomer.execute("customerId")).thenReturn(Observable.just(true))
        // when
        val result = isAddTransactionRestricted.execute("customerId").test()

        // then
        result.assertValue { it }
    }
}
