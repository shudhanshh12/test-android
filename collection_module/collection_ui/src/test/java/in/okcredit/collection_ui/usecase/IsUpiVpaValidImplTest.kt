package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class IsUpiVpaValidImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val isUpiVpaValid = IsUpiVpaValidImpl(collectionRepository, { getActiveBusinessId })

    @Test
    fun `check if upi is valid`() {
        // Given
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.validatePaymentAddress("upi", "8882946897@ybl", businessId))
            .thenReturn(Single.just(Pair(true, "name")))

        // When
        val testObserver = isUpiVpaValid.execute("8882946897@ybl").test()

        // Then
        testObserver.assertValue(Pair(true, "name"))
        verify(collectionRepository).validatePaymentAddress("upi", "8882946897@ybl", businessId)
    }
}
