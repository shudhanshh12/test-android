package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class GetLatestOnlinePaymentDateTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getLatestOnlinePaymentDate = GetLatestOnlinePaymentDate(
        { collectionRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute() should return latest date`() {
        val businessId = "business-id"
        val dateTime: DateTime = mock()
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getLatestOnlinePaymentDate(businessId)).thenReturn(dateTime)

        val testObserver = getLatestOnlinePaymentDate.execute().test()

        testObserver.assertValue(dateTime)
    }
}
