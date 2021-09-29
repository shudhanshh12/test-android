package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class MarkCollectionSharedTest {

    private val collectionApi = mock<CollectionRepository>()
    private val getActiveBusinessId = mock<GetActiveBusinessId>()
    private val markCollectionShared = MarkCollectionShared(collectionRepository = { collectionApi }, getActiveBusinessId = { getActiveBusinessId })

    @Test
    fun `execute() should call insertCollectionShareInfo on collectionApi`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionApi.insertCollectionShareInfo(any(), eq(businessId))).thenReturn(Completable.complete())

        val testObserver = markCollectionShared.execute("").test()

        testObserver.assertComplete()
        verify(collectionApi).insertCollectionShareInfo(any(), eq(businessId))
    }
}
