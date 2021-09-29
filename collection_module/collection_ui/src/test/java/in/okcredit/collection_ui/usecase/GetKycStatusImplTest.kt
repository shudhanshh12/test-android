package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetKycStatusImplTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    private val getKycStatusImpl = GetKycStatusImpl({ collectionRepository }, { getActiveBusinessId })

    companion object {
        private const val KYC_FEATURE = "collections_kyc"
    }

    @Before
    fun setup() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `execute() should return kyc status not set`() {
        val kycStatus = KycStatus.NOT_SET
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just(kycStatus.value))

        val testObserver = getKycStatusImpl.execute().test()

        testObserver.assertValues(kycStatus)

        testObserver.dispose()
    }

    @Test
    fun `execute() should return kyc status pending`() {
        val kycStatus = KycStatus.PENDING
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just(kycStatus.value))

        val testObserver = getKycStatusImpl.execute().test()

        testObserver.assertValues(kycStatus)

        testObserver.dispose()
    }

    @Test
    fun `execute() should return kyc status failed`() {
        val kycStatus = KycStatus.FAILED
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just(kycStatus.value))

        val testObserver = getKycStatusImpl.execute().test()

        testObserver.assertValues(kycStatus)

        testObserver.dispose()
    }

    @Test
    fun `execute() should return kyc status complete`() {
        val kycStatus = KycStatus.COMPLETE
        whenever(collectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just(kycStatus.value))

        val testObserver = getKycStatusImpl.execute().test()

        testObserver.assertValues(kycStatus)

        testObserver.dispose()
    }
}
