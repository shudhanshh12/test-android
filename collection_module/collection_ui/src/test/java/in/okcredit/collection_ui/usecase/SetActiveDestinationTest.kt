package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class SetActiveDestinationTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val setActiveDestination = SetActiveDestination(collectionRepository, { getActiveBusinessId })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `setActiveDestination successful when async is true`() {
        val collectionMerchantProfile: CollectionMerchantProfile = mock()
        val response: ApiMessages.MerchantCollectionProfileResponse = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setActiveDestination(collectionMerchantProfile, true, "", businessId))
            .thenReturn(Single.just(response))

        val testObserver = setActiveDestination.execute(collectionMerchantProfile, true, "").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        verify(collectionRepository).setActiveDestination(collectionMerchantProfile, true, "", businessId)

        testObserver.dispose()
    }

    @Test
    fun `setActiveDestination returns  error when async is true`() {
        val mockError: Exception = mock()
        val collectionMerchantProfile: CollectionMerchantProfile = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setActiveDestination(collectionMerchantProfile, true, "", businessId))
            .thenReturn(Single.error(mockError))

        val testObserver = setActiveDestination.execute(collectionMerchantProfile, true, "").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(mockError)
        )
        verify(collectionRepository).setActiveDestination(collectionMerchantProfile, true, "", businessId)

        testObserver.dispose()
    }

    @Test
    fun `setActiveDestination  successful when async is false`() {
        val collectionMerchantProfile: CollectionMerchantProfile = mock()
        val response: ApiMessages.MerchantCollectionProfileResponse = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setActiveDestination(collectionMerchantProfile, false, "", businessId))
            .thenReturn(Single.just(response))

        val testObserver = setActiveDestination.execute(collectionMerchantProfile, false, "").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(response)
        )

        verify(collectionRepository).setActiveDestination(collectionMerchantProfile, false, "", businessId)

        testObserver.dispose()
    }

    @Test
    fun `setActiveDestination returns  error when async is false`() {
        val mockError: Exception = mock()
        val collectionMerchantProfile: CollectionMerchantProfile = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setActiveDestination(collectionMerchantProfile, false, "", businessId))
            .thenReturn(Single.error(mockError))

        val testObserver = setActiveDestination.execute(collectionMerchantProfile, false, "").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(mockError)
        )
        verify(collectionRepository).setActiveDestination(collectionMerchantProfile, false, "", businessId)

        testObserver.dispose()
    }
}
