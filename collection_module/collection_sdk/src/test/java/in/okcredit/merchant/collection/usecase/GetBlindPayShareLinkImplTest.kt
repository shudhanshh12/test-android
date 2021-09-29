package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.merchant.collection.CollectionRepositoryImpl
import `in`.okcredit.merchant.collection.CollectionTestData
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

class GetBlindPayShareLinkImplTest {
    private val collectionRepositoryImpl: CollectionRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getBlindPayShareLinkImpl = GetBlindPayShareLinkImpl(
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
    fun `execute return success with Share Link when payment id provided `() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(CollectionTestData.BUSINESS_ID))
        whenever(
            collectionRepositoryImpl.getBlindPayShareLink(
                CollectionTestData.PAYMENT_ID,
                CollectionTestData.BUSINESS_ID
            )
        ).thenReturn(
            Single.just(
                CollectionTestData.BLIND_PAY_SHARE_LINK_WITH_PAYMENT_ID_RESPONSE
            )
        )

        val testObserver = getBlindPayShareLinkImpl.execute(CollectionTestData.PAYMENT_ID).test()
        testObserver.assertValue(CollectionTestData.SHARE_LINK)
        verify(collectionRepositoryImpl).getBlindPayShareLink(
            CollectionTestData.PAYMENT_ID,
            CollectionTestData.BUSINESS_ID
        )

        testObserver.dispose()
    }
}
