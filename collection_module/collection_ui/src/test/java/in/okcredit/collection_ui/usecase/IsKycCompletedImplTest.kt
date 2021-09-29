package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class IsKycCompletedImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var isKycCompletedImpl: IsKycCompletedImpl
    private val businessId = "businessId"

    @Before
    fun setUp() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        isKycCompletedImpl = IsKycCompletedImpl({ collectionRepository }, { getActiveBusinessId })
    }

    @Test
    fun `when kyc gets completed`() {
        // given
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just("COMPLETE"))

        // when
        val result = isKycCompletedImpl.execute().test()

        // then
        result.assertValues(
            true
        )
    }

    @Test
    fun `when kyc gets failed`() {
        // given
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just("FAILED"))

        // when
        val result = isKycCompletedImpl.execute().test()

        // then
        result.assertValues(
            false
        )
    }

    @Test
    fun `when kyc gets pending`() {
        // given
        whenever(collectionRepository.getKycStatus(businessId)).thenReturn(Observable.just("PENDING"))

        // when
        val result = isKycCompletedImpl.execute().test()

        // then
        result.assertValues(
            false
        )
    }
}
