package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.justRun
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetSupplierCollectionProfileWithSyncImplTest {

    private val collectionRepository: CollectionRepository = mock()
    private val collectionSyncer: CollectionSyncer = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSupplierCollectionProfileWithSyncImpl = GetSupplierCollectionProfileWithSyncImpl(
        collectionRepository = { collectionRepository },
        collectionSyncer = { collectionSyncer },
        getActiveBusinessId = { getActiveBusinessId }
    )

    @Test
    fun `execute() should schedule sync and get collection profile if async true`() {
        val supplierId = "supplier-id"
        val businessId = "business-id"
        val profile = mock<CollectionCustomerProfile>()
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        justRun { (collectionSyncer).scheduleCollectionProfileForSupplier(supplierId, businessId) }
        whenever(collectionRepository.getSupplierCollectionProfile(supplierId, businessId)).thenReturn(
            Observable.create {
                it.onNext(profile)
            }
        )

        val testObserver = getSupplierCollectionProfileWithSyncImpl.execute(supplierId, true).test()

        testObserver.assertValue(profile)
        io.mockk.verify { (collectionSyncer).scheduleCollectionProfileForSupplier(supplierId, businessId) }
        verify(collectionRepository).getSupplierCollectionProfile(supplierId, businessId)
    }

    @Test
    fun `execute() should schedule sync and get collection profile if async false`() {
        runBlocking {
            val supplierId = "supplier-id"
            val businessId = "business-id"
            val profile = mock<CollectionCustomerProfile>()
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            coJustRun { (collectionSyncer).executeSyncCollectionProfileForSupplier(supplierId, businessId) }
            whenever(collectionRepository.getSupplierCollectionProfile(supplierId, businessId)).thenReturn(
                Observable.create {
                    it.onNext(profile)
                }
            )

            getSupplierCollectionProfileWithSyncImpl.execute(supplierId, false).test().apply {
                awaitCount(1)
                assertValue(profile)
                dispose()
            }

            coVerify { (collectionSyncer).executeSyncCollectionProfileForSupplier(supplierId, businessId) }
            verify(collectionRepository).getSupplierCollectionProfile(supplierId, businessId)
        }
    }
}
