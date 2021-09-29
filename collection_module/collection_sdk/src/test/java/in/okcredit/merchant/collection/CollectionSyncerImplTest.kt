package `in`.okcredit.merchant.collection

import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.merchant.collection.CollectionTestData.COLLECTION_MERCHANT_PROFILE
import `in`.okcredit.merchant.collection.CollectionTestData.CUSTOMER_COLLECTION_PROFILE_1
import `in`.okcredit.merchant.collection.CollectionTestData.CUSTOMER_COLLECTION_PROFILE_2
import `in`.okcredit.merchant.collection.CollectionTestData.SUPPLIER_COLLECTION_PROFILE_1
import `in`.okcredit.merchant.collection.CollectionTestData.SUPPLIER_COLLECTION_PROFILE_2
import `in`.okcredit.merchant.collection.server.CollectionRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.verify
import io.mockk.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager

class CollectionSyncerImplTest {

    private val okcWorkManager: OkcWorkManager = mockk()
    private val localSource: CollectionLocalSource = mockk()
    private val remoteSource: CollectionRemoteSource = mockk()
    private val collectionSyncTracker: CollectionSyncTracker = mockk(relaxUnitFun = true)
    private val getActiveBusinessId: GetActiveBusinessId = mockk()

    private lateinit var collectionSyncerImpl: CollectionSyncerImpl

    private val businessId = CollectionTestData.BUSINESS_ID

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()

        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit

        every { (localSource.getLastSyncOnlineCollectionsTime(businessId)) } returns (DateTime(120120012))
        collectionSyncerImpl = CollectionSyncerImpl(
            workManager = { okcWorkManager },
            localSource = { localSource },
            remoteSource = { remoteSource },
            collectionSyncTracker = { collectionSyncTracker },
            getActiveBusinessId = { getActiveBusinessId }
        )

        coEvery { getActiveBusinessId.execute() } returns Single.just(businessId)
        coEvery { getActiveBusinessId.thisOrActiveBusinessId(null) } returns Single.just(businessId)
        coEvery { getActiveBusinessId.thisOrActiveBusinessId(businessId) } returns Single.just(businessId)
    }

    @Test
    fun `sync customer collection test`() {
        runBlocking {
            every { localSource.getLastSyncCustomerCollectionsTime(businessId) } returns Single.just(10000L)
            coEvery {
                remoteSource.getCustomerCollections(
                    null,
                    11L,
                    businessId
                )
            } returns listOf(CollectionTestData.COLLECTION1, CollectionTestData.COLLECTION2)
            every {
                localSource.putCollections(
                    listOf(
                        CollectionTestData.COLLECTION1,
                        CollectionTestData.COLLECTION2
                    ),
                    businessId
                )
            } returns Completable.complete()

            every {
                localSource.setLastSyncCustomerCollectionsTime(
                    CollectionTestData.CURRENT_TIME.plusDays(1).millis,
                    businessId
                )
            } returns Completable.complete()

            collectionSyncerImpl.executeSyncCustomerCollections(businessId)

            coVerify { (remoteSource).getCustomerCollections(null, 11L, businessId) }
            io.mockk.verify {
                (localSource).putCollections(
                    listOf(
                        CollectionTestData.COLLECTION1,
                        CollectionTestData.COLLECTION2
                    ),
                    businessId
                )
            }
            io.mockk.verify {
                (localSource).setLastSyncCustomerCollectionsTime(
                    CollectionTestData.CURRENT_TIME.plusDays(1).millis,
                    businessId
                )
            }
        }
    }

    @Test
    fun `sync customer collection test empty list`() {
        runBlocking {
            every { localSource.getLastSyncCustomerCollectionsTime(businessId) } returns Single.just(10000L)
            coEvery {
                remoteSource.getCustomerCollections(
                    null,
                    11L,
                    businessId
                )
            } returns emptyList()

            collectionSyncerImpl.executeSyncCustomerCollections()

            coVerify { (remoteSource).getCustomerCollections(null, 11L, businessId) }
        }
    }

    @Test
    fun `sync supplier collection test empty list`() {
        runBlocking {
            every { localSource.getLastSyncSupplierCollectionsTime(businessId) } returns Single.just(10000L)
            coEvery {
                remoteSource.getSupplierCollections(
                    null,
                    11L,
                    businessId
                )
            } returns emptyList()

            collectionSyncerImpl.executeSyncSupplierCollections()

            coVerify { (remoteSource).getSupplierCollections(null, 11L, businessId) }
        }
    }

    @Test
    fun `sync supplier collection test`() {
        runBlocking {
            coEvery {
                remoteSource.getSupplierCollections(
                    null,
                    11L,
                    businessId
                )
            } returns listOf(CollectionTestData.COLLECTION1, CollectionTestData.COLLECTION2)

            every { localSource.getLastSyncSupplierCollectionsTime(businessId) } returns Single.just(10000L)
            every {
                localSource.putCollections(
                    listOf(
                        CollectionTestData.COLLECTION1,
                        CollectionTestData.COLLECTION2
                    ),
                    businessId
                )
            } returns Completable.complete()

            every {
                localSource.setLastSyncSupplierCollectionsTime(
                    CollectionTestData.CURRENT_TIME.plusDays(1).millis,
                    businessId
                )
            } returns Completable.complete()

            collectionSyncerImpl.executeSyncSupplierCollections()

            coVerify { (remoteSource).getSupplierCollections(null, 11L, businessId) }
            coVerify {
                localSource.putCollections(
                    listOf(
                        CollectionTestData.COLLECTION1,
                        CollectionTestData.COLLECTION2
                    ),
                    businessId
                )
            }
            coVerify {
                (localSource).setLastSyncSupplierCollectionsTime(
                    CollectionTestData.CURRENT_TIME.plusDays(1).millis,
                    businessId
                )
            }
        }
    }

    @Test
    fun `sync collection profile test success`() {
        runBlocking {
            val response = CollectionProfiles(
                collectionMerchantProfile = COLLECTION_MERCHANT_PROFILE,
                collectionCustomerProfiles = listOf(CUSTOMER_COLLECTION_PROFILE_1, CUSTOMER_COLLECTION_PROFILE_2),
                supplierCollectionProfiles = listOf(SUPPLIER_COLLECTION_PROFILE_1, SUPPLIER_COLLECTION_PROFILE_2)
            )
            justRun { collectionSyncTracker.trackExecuteSyncMerchantProfile() }
            coEvery { remoteSource.getCollectionProfiles(businessId) } returns response
            every { localSource.setCollectionMerchantProfile(COLLECTION_MERCHANT_PROFILE) } returns Completable.complete()
            every {
                localSource.putCustomerCollectionProfiles(
                    listOf(
                        CUSTOMER_COLLECTION_PROFILE_1,
                        CUSTOMER_COLLECTION_PROFILE_2
                    ),
                    businessId
                )
            } returns Completable.complete()
            every {
                localSource.putSupplierCollectionProfiles(
                    listOf(
                        SUPPLIER_COLLECTION_PROFILE_1,
                        SUPPLIER_COLLECTION_PROFILE_2
                    ),
                    businessId
                )
            } returns Completable.complete()

            collectionSyncerImpl.executeSyncCollectionProfile(businessId)

            coVerify { (localSource).setCollectionMerchantProfile(COLLECTION_MERCHANT_PROFILE) }
            coVerify {
                (localSource).putCustomerCollectionProfiles(
                    listOf(
                        CUSTOMER_COLLECTION_PROFILE_1,
                        CUSTOMER_COLLECTION_PROFILE_2
                    ),
                    businessId
                )
            }
            coVerify {
                (localSource).putSupplierCollectionProfiles(
                    listOf(
                        SUPPLIER_COLLECTION_PROFILE_1,
                        SUPPLIER_COLLECTION_PROFILE_2
                    ),
                    businessId
                )
            }
        }
    }

    @Test
    fun `sync collection profile test address_not_found`() {
        runBlocking {
            coEvery { remoteSource.getCollectionProfiles(businessId) } throws (CollectionServerErrors.AddressNotFound())
            every { localSource.clearCollectionMerchantProfile(businessId) } returns Completable.complete()
            collectionSyncerImpl.executeSyncCollectionProfile()

            coVerify { localSource.clearCollectionMerchantProfile(businessId) }
        }
    }

    @Test
    fun `sync customer collection profile test success`() {
        runBlocking {
            coEvery {
                remoteSource.getCollectionCustomerProfile(CUSTOMER_COLLECTION_PROFILE_1.accountId, businessId)
            } returns CUSTOMER_COLLECTION_PROFILE_1

            every {
                localSource.putCustomerCollectionProfile(
                    CUSTOMER_COLLECTION_PROFILE_1,
                    businessId
                )
            } returns Completable.complete()
            collectionSyncerImpl.executeSyncCollectionProfileForCustomer(CUSTOMER_COLLECTION_PROFILE_1.accountId)

            coVerify { (localSource).putCustomerCollectionProfile(CUSTOMER_COLLECTION_PROFILE_1, businessId) }
        }
    }

    @Test
    fun `sync supplier collection profile test feature not enabled`() {
        runBlocking {
            collectionSyncerImpl.executeSyncCollectionProfileForSupplier(CUSTOMER_COLLECTION_PROFILE_1.accountId)

            coVerify(exactly = 0) {
                (remoteSource).getCollectionSupplierProfile(
                    CUSTOMER_COLLECTION_PROFILE_1.accountId,
                    businessId
                )
            }
        }
    }

    @Test
    fun `sync supplier collection profile test success`() {
        runBlocking {
            coEvery {
                remoteSource.getCollectionSupplierProfile(CUSTOMER_COLLECTION_PROFILE_1.accountId, businessId)
            } returns (CUSTOMER_COLLECTION_PROFILE_1)
            every {
                localSource.putSupplierCollectionProfile(
                    CUSTOMER_COLLECTION_PROFILE_1,
                    businessId
                )
            } returns Completable.complete()
            collectionSyncerImpl.executeSyncCollectionProfileForSupplier(
                CUSTOMER_COLLECTION_PROFILE_1.accountId,
                businessId
            )

            coVerify {
                (remoteSource).getCollectionSupplierProfile(
                    CUSTOMER_COLLECTION_PROFILE_1.accountId,
                    businessId
                )
            }
            coVerify { (localSource).putSupplierCollectionProfile(CUSTOMER_COLLECTION_PROFILE_1, businessId) }
        }
    }
}
