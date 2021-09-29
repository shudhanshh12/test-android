package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.rewards.contract.RewardsSyncer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException

class SetCollectionDestinationImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val rewardsSyncer: RewardsSyncer = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val tracker: Tracker = mock()
    private lateinit var setCollectionDestinationImpl: SetCollectionDestinationImpl

    companion object {
        val collectionMerchantProfile = CollectionMerchantProfile(
            merchant_id = "merchant_id",
            name = null,
            payment_address = "",
            type = "",
            merchant_vpa = null
        )

        val merchantCollectionProfileResponse = ApiMessages.MerchantCollectionProfileResponse(
            customers = listOf(),
            suppliers = listOf(),
            destination = ApiMessages.Destination(
                mobile = null,
                name = null,
                paymentAddress = "",
                type = "",
                upiVpa = null,
            ),
            merchantId = "merchant_id",
            merchantVpa = "merchant_vpa",
            eta = 10L
        )
    }

    @Before
    fun setUp() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        setCollectionDestinationImpl = SetCollectionDestinationImpl(
            collectionRepository, rewardsSyncer, tracker, { getActiveBusinessId }
        )
    }

    @Test
    fun `test given collectionmerchant profile is added`() {
        // given
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setActiveDestination(collectionMerchantProfile, false, "", businessId))
            .thenReturn(Single.just(merchantCollectionProfileResponse))
        whenever(rewardsSyncer.scheduleEverything(businessId)).thenReturn(Completable.complete())

        // when
        val result = setCollectionDestinationImpl.execute(collectionMerchantProfile).test()

        // then
        result.assertValue {
            collectionMerchantProfile == it
        }
    }

    @Test
    fun `test for error handling`() {
        // given
        val mockError = Throwable()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setActiveDestination(collectionMerchantProfile, false, "", businessId))
            .thenReturn(Single.error(mockError))
        whenever(rewardsSyncer.scheduleEverything(businessId))
            .thenReturn(Completable.complete())

        // when
        val testObserver = setCollectionDestinationImpl.execute(collectionMerchantProfile).test()

        // then
        testObserver.assertError(mockError)
        io.mockk.verify { RecordException.recordException(mockError) }
    }
}
