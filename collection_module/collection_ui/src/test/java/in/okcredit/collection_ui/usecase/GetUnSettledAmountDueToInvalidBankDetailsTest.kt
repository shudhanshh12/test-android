package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetUnSettledAmountDueToInvalidBankDetailsTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getUnSettledAmountDueToInvalidBankDetails =
        GetUnSettledAmountDueToInvalidBankDetails(
            { collectionRepository },
            { getActiveBusinessId }
        )

    @Test
    fun `execute and return a value`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getUnsettledOnlinePaymentAmount("EP001", 11, businessId))
            .thenReturn(Observable.just(2.0))

        val testObserver =
            getUnSettledAmountDueToInvalidBankDetails.execute("EP001", 11).test()

        verify(collectionRepository).getUnsettledOnlinePaymentAmount("EP001", 11, businessId)
        testObserver.assertValue(2.0)
        testObserver.dispose()
    }
}
