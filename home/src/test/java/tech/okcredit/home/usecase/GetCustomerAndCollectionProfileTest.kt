package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.usecase.ImmutableConflictHelper
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.home.TestData

class GetCustomerAndCollectionProfileTest {
    private val mockCollectionRepository: CollectionRepository = mock()
    private val mockGetCustomer: GetCustomer = mock()
    private val mockCollectionSyncer: CollectionSyncer = mock()
    private val mockImmutableConflictHelper: ImmutableConflictHelper = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCustomerAndCollectionProfile = GetCustomerAndCollectionProfile(
        { mockCollectionRepository },
        { mockCollectionSyncer },
        { mockGetCustomer },
        { mockImmutableConflictHelper },
        { getActiveBusinessId }
    )

    @Test
    fun `Should return CollectionCustomerProfile null For Non Collection activated users`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(mockCollectionRepository.isCollectionActivated()).thenReturn(Observable.just(false))
        doNothing().whenever(mockCollectionSyncer)
            .scheduleCollectionProfileForCustomer(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        whenever(mockGetCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))
        whenever(mockCollectionRepository.getCollectionCustomerProfile(TestData.CUSTOMER.id, TestData.BUSINESS_ID))
            .thenReturn(Observable.just(TestData.COLLECTION_CUSTOMER_PROFILE))

        val testObserver = getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id).test()

        // then
        testObserver.assertValue(GetCustomerAndCollectionProfile.Response(TestData.CUSTOMER))

        testObserver.dispose()
    }

    @Test
    fun `Should return Customer and CollectionCustomerProfile on success of every API For Collection activated users`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(mockCollectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        doNothing().whenever(mockCollectionSyncer)
            .scheduleCollectionProfileForCustomer(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        whenever(mockGetCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))
        whenever(mockCollectionRepository.getCollectionCustomerProfile(TestData.CUSTOMER.id, TestData.BUSINESS_ID))
            .thenReturn(Observable.just(TestData.COLLECTION_CUSTOMER_PROFILE))

        val testObserver = getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id).test()

        // then
        testObserver.assertValue(
            GetCustomerAndCollectionProfile.Response(
                TestData.CUSTOMER,
                TestData.COLLECTION_CUSTOMER_PROFILE,
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `Should return Error if getCollectionCustomerProfile() fails For Collection activated users`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(mockCollectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        doNothing().whenever(mockCollectionSyncer)
            .scheduleCollectionProfileForCustomer(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        whenever(mockGetCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))
        whenever(mockCollectionRepository.getCollectionCustomerProfile(TestData.CUSTOMER.id, TestData.BUSINESS_ID))
            .thenReturn(Observable.error(TestData.ERROR))

        val testObserver = getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id).test()

        // then
        testObserver.assertError(TestData.ERROR)

        testObserver.dispose()
    }

    @Test
    fun `Should return Error if getCustomer() fails For Collection activated users`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(mockCollectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        doNothing().whenever(mockCollectionSyncer)
            .scheduleCollectionProfileForCustomer(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        whenever(mockGetCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.error(TestData.ERROR))
        whenever(mockCollectionRepository.getCollectionCustomerProfile(TestData.CUSTOMER.id, TestData.BUSINESS_ID))
            .thenReturn(Observable.just(TestData.COLLECTION_CUSTOMER_PROFILE))

        val testObserver = getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id).test()

        // then
        testObserver.assertError(TestData.ERROR)

        testObserver.dispose()
    }

    @Test
    fun `Should return latest value of customer if getCustomer() emitting multiple times For Collection activated users`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(mockCollectionRepository.isCollectionActivated()).thenReturn(Observable.just(true))
        doNothing().whenever(mockCollectionSyncer)
            .scheduleCollectionProfileForCustomer(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        whenever(mockGetCustomer.execute(TestData.CUSTOMER.id))
            .thenReturn(Observable.just(TestData.CUSTOMER, TestData.CUSTOMER_2))

        whenever(mockCollectionRepository.getCollectionCustomerProfile(TestData.CUSTOMER.id, TestData.BUSINESS_ID))
            .thenReturn(Observable.just(TestData.COLLECTION_CUSTOMER_PROFILE))

        val testObserver = getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id).test()

        // then
        testObserver.assertValues(
            GetCustomerAndCollectionProfile.Response(
                TestData.CUSTOMER,
                TestData.COLLECTION_CUSTOMER_PROFILE,
            ),
            GetCustomerAndCollectionProfile.Response(
                TestData.CUSTOMER_2,
                TestData.COLLECTION_CUSTOMER_PROFILE,
            )
        )

        testObserver.dispose()
    }
}
