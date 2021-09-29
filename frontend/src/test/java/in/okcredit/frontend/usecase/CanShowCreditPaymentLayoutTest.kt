package `in`.okcredit.frontend.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.usecase.CanShowCreditPaymentLayout
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
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS

class CanShowCreditPaymentLayoutTest {
    private val keyValService: KeyValService = mock()
    private val ab: AbRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var canShowCreditPaymentLayout: CanShowCreditPaymentLayout
    private val businessId = "businessId"

    @Before
    fun setUp() {
        canShowCreditPaymentLayout = CanShowCreditPaymentLayout({ keyValService }, { ab }, { getActiveBusinessId })

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(getActiveBusinessId.thisOrActiveBusinessId(any())).thenReturn(Single.just(businessId))
    }

    @Test
    fun whenSingleListFeatureEnabled() {
        // given
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(keyValService.contains(eq(PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS), any()))
            .thenReturn(Single.just(true))
        whenever(keyValService[eq(PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS), any()])
            .thenReturn(Observable.just("customerid"))

        // when
        val result = canShowCreditPaymentLayout.execute("customerId").test()

        // then
        result.assertValue { it }
    }

    @Test
    fun `whenSingleListFeatureEnabled but RestrictedCustomer return false`() {
        // given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(keyValService.contains(eq(PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS), any())).thenReturn(
            Single.just(
                true
            )
        )
        whenever(keyValService[eq(PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS), any()]).thenReturn(Observable.just("customerId"))

        // when
        val result = canShowCreditPaymentLayout.execute("customerId").test()

        // then
        result.assertValue { it.not() }
    }

    @Test
    fun `true whenSingleListFeatureEnabled keyAddTxnRestricted false`() {
        // given
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId)).thenReturn(Observable.just(true))
        whenever(keyValService.contains(eq(PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS), any())).thenReturn(
            Single.just(false)
        )

        // when
        val result = canShowCreditPaymentLayout.execute("customerId").test()

        // then
        result.assertValue { it }
    }

    @Test
    fun `SingleList feature is not enabled`() {
        // given
        whenever(ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId))
            .thenReturn(Observable.just(false))

        // when
        val result = canShowCreditPaymentLayout.execute("customerId").test()

        // then
        result.assertValue { it }
    }
}
