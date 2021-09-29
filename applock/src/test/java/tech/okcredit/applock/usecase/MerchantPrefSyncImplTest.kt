package tech.okcredit.applock.usecase

import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.applock.MerchantPrefSyncImpl

class MerchantPrefSyncImplTest {
    private val businessApi: BusinessRepository = mock()
    private val individualRepository: IndividualRepository = mock()
    private val checkFingerPrintLockAvailability: CheckFingerPrintLockAvailability = mock()
    private val checkIsFingerPrintEnabled: GetMerchantFingerprintPreference = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val merchantPrefSyncImpl =
        MerchantPrefSyncImpl(
            { businessApi },
            { individualRepository },
            { checkFingerPrintLockAvailability },
            { checkIsFingerPrintEnabled },
            { getActiveBusinessId }
        )

    @Test
    fun `when executeSyncMerchant and executeSyncMerchantPreferences return complete`() {
        // given
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(businessApi.executeSyncBusiness(businessId)).thenReturn(Completable.complete())

        // when
        val result = merchantPrefSyncImpl.execute().test()

        // then
        result.assertComplete()
    }

    @Test
    fun `when checkFingerPrintAvailabilityTrue`() {
        // given
        whenever(checkFingerPrintLockAvailability.execute()).thenReturn(Observable.just(true))

        // when
        val result = merchantPrefSyncImpl.checkFingerPrintAvailability().test()

        // then
        result.assertValue(true)
    }

    @Test
    fun `when checkFingerPrintAvailabilityFalse`() {
        // given
        whenever(checkFingerPrintLockAvailability.execute()).thenReturn(Observable.just(false))

        // when
        val result = merchantPrefSyncImpl.checkFingerPrintAvailability().test()

        // then
        result.assertValue(false)
    }

    @Test
    fun `when checkIsFingerPrintEnabledTrue`() {
        // given
        whenever(checkIsFingerPrintEnabled.execute()).thenReturn(Observable.just(true))

        // when
        val result = merchantPrefSyncImpl.checkFingerPrintEnable().test()

        // then
        result.assertValue(true)
    }

    @Test
    fun `when checkIsFingerPrintEnabledFalse`() {
        // given
        whenever(checkIsFingerPrintEnabled.execute()).thenReturn(Observable.just(false))

        // when
        val result = merchantPrefSyncImpl.checkFingerPrintEnable().test()

        // then
        result.assertValue(false)
    }
}
