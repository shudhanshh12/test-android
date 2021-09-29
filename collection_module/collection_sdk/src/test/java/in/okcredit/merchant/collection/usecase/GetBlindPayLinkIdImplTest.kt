package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.merchant.collection.CollectionRepositoryImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class GetBlindPayLinkIdImplTest {
    private val collectionRepositoryImpl: CollectionRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getBlindPayLinkIdImpl = GetBlindPayLinkIdImpl(
        { collectionRepositoryImpl },
        { getActiveBusinessId }
    )

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `execute return success with linkId`() {
        val accountId = "asdf123456313"
        val linkId = "test_link_id"
        val businessId = "businessId"
        val response = ApiMessages.BlindPayCreateLinkResponse(linkId)

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepositoryImpl.getBlindPayLinkId(accountId, businessId)).thenReturn(Single.just(response))

        val testObserver = getBlindPayLinkIdImpl.execute(accountId).test()
        testObserver.assertValue(linkId)
        verify(collectionRepositoryImpl).getBlindPayLinkId(accountId, businessId)

        testObserver.dispose()
    }
}
