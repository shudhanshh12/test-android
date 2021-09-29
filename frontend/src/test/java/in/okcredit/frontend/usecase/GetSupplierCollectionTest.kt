package `in`.okcredit.frontend.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
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
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetSupplierCollectionTest {
    private val collectionRepository: CollectionRepository = mock()
    private val collectionSyncer: CollectionSyncer = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSupplierCollection: GetSupplierCollection =
        GetSupplierCollection(collectionRepository, collectionSyncer, { getActiveBusinessId })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `excute() return completable`() {

        val collection: `in`.okcredit.collection.contract.Collection = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        doNothing().whenever(collectionSyncer)
            .scheduleSyncCollections(CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS, "supplier_screen", businessId)

        whenever(collectionRepository.getCollection("collection_id", businessId)).thenReturn(
            Observable.just(collection)
        )

        val testObserver = getSupplierCollection.execute("collection_id").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(
                collection
            )
        )

        verify(collectionSyncer).scheduleSyncCollections(
            CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS,
            "supplier_screen",
            businessId
        )
        verify(collectionRepository).getCollection("collection_id", businessId)

        testObserver.dispose()
    }

    @Test
    fun `execute() getCollection return  error`() {

        val mockError: Exception = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        doNothing().whenever(collectionSyncer)
            .scheduleSyncCollections(CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS, "supplier_screen", businessId)

        whenever(collectionRepository.getCollection("collection_id", businessId)).thenReturn(
            Observable.error(mockError)
        )

        val testObserver = getSupplierCollection.execute("collection_id").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(
                mockError
            )
        )

        verify(collectionSyncer).scheduleSyncCollections(
            CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS,
            "supplier_screen",
            businessId
        )
        verify(collectionRepository).getCollection("collection_id", businessId)

        testObserver.dispose()
    }

    @Test
    fun `excute() scheduleSyncCollections return  error`() {

        val mockError: Exception = mock()
        val collection: `in`.okcredit.collection.contract.Collection = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        doNothing().whenever(collectionSyncer)
            .scheduleSyncCollections(CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS, "supplier_screen", businessId)

        whenever(collectionRepository.getCollection("collection_id", businessId)).thenReturn(
            Observable.error(mockError)
        )

        val testObserver = getSupplierCollection.execute("collection_id").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(
                mockError
            )
        )

        verify(collectionSyncer).scheduleSyncCollections(
            CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS,
            "supplier_screen",
            businessId
        )
        verify(collectionRepository).getCollection("collection_id", businessId)

        testObserver.dispose()
    }
}
