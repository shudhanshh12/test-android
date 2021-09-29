package `in`.okcredit.merchant.collection.store.database

import `in`.okcredit.merchant.collection.CollectionTestData
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class CollectionDataBaseTest {

    private lateinit var collectionDb: CollectionDataBase
    private val businessId = "businessId"

    @Before
    fun setUp() {
        collectionDb = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CollectionDataBase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        collectionDb.close()
    }

    @Test
    fun `insert collection test`() {
        collectionDb.collectionDataBaseDao()
            .insertCollections(
                CollectionTestData.COLLECTION_ENTITY1
            )

        val testObserver =
            collectionDb.collectionDataBaseDao().getCollection(CollectionTestData.COLLECTION_ENTITY1.id).test()
                .awaitCount(1)
        testObserver.assertValue { it.id == CollectionTestData.COLLECTION_ENTITY1.id }
    }

    @Test
    fun `list collection test`() {
        collectionDb.collectionDataBaseDao().insertCollections(CollectionTestData.COLLECTION_ENTITY1)

        val testObserver = collectionDb.collectionDataBaseDao().listCollections(CollectionTestData.BUSINESS_ID).test().awaitCount(1)

        testObserver.assertValue {
            it.size == 1 &&
                it[0].id == CollectionTestData.COLLECTION_ENTITY1.id
        }
    }

    @Test
    fun `list listCollectionsOfCustomer test`() {
        collectionDb.collectionDataBaseDao()
            .insertCollections(
                CollectionTestData.COLLECTION_ENTITY1,
                CollectionTestData.COLLECTION_ENTITY2
            )

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionsOfCustomer("customer_id_2", CollectionTestData.BUSINESS_ID).test()
                .awaitCount(1)

        testObserver.assertValue {
            it[0].customer_id == CollectionTestData.COLLECTION_ENTITY2.customer_id
        }
    }

    @Test
    fun `list getCollection test`() {
        collectionDb.collectionDataBaseDao()
            .insertCollections(
                CollectionTestData.COLLECTION_ENTITY1,
                CollectionTestData.COLLECTION_ENTITY2
            )

        val testObserver =
            collectionDb.collectionDataBaseDao().getCollection(CollectionTestData.COLLECTION_ENTITY2.id).test()
                .awaitCount(1)

        testObserver.assertValue {
            it.id == CollectionTestData.COLLECTION_ENTITY2.id
        }
    }

    @Test
    fun `list deleteAllCollections test`() {
        collectionDb.collectionDataBaseDao().insertCollections(
            CollectionTestData.COLLECTION_ENTITY1,
            CollectionTestData.COLLECTION_ENTITY2
        )

        collectionDb.collectionDataBaseDao().deleteAllCollections().test().await()

        val testObserver = collectionDb.collectionDataBaseDao().listCollections(CollectionTestData.BUSINESS_ID).test().awaitCount(1)

        testObserver.assertValue {
            it.isEmpty()
        }
    }

    @Test
    fun ` setCollectionsProfile test`() {
        collectionDb.collectionDataBaseDao().setCollectionsProfile(
            CollectionTestData.COLLECTION_PROFILE_ENTITY1
        ).test().assertComplete()

        val testObserver =
            collectionDb.collectionDataBaseDao().getCollectionsProfile(businessId).test().awaitCount(1)

        testObserver.assertValue { it[0].merchant_id == CollectionTestData.COLLECTION_PROFILE_ENTITY1.merchant_id }
    }

    @Test
    fun ` getCollectionsProfile test`() {
        collectionDb.collectionDataBaseDao().setCollectionsProfile(
            CollectionTestData.COLLECTION_PROFILE_ENTITY1
        ).test().assertComplete()

        val testObserver =
            collectionDb.collectionDataBaseDao().getCollectionsProfile(businessId).test()
                .awaitCount(1)
        testObserver.assertValue { it.size == 1 }
    }

    @Test
    fun ` deleteMerchantProfile test`() {
        collectionDb.collectionDataBaseDao().setCollectionsProfile(
            CollectionTestData.COLLECTION_PROFILE_ENTITY1
        )

        collectionDb.collectionDataBaseDao().deleteMerchantProfileForBusinessId(businessId).test().assertComplete()

        val testObserver = collectionDb.collectionDataBaseDao().getCollectionsProfile(businessId).test()
            .awaitCount(1)
        testObserver.assertValue { it.isEmpty() }
    }

    @Test
    fun ` listCollectionCustomerProfiles test`() {
        collectionDb.collectionDataBaseDao().insertCustomerCollectionProfile(
            CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1
        )

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionCustomerProfiles(
                CollectionTestData.BUSINESS_ID
            ).test().awaitCount(1)

        testObserver.assertValue {
            it.size == 1 && it[0].customerId == CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1.customerId
        }
    }

    @Test
    fun ` insertCollectionCustomerProfile test`() {
        collectionDb.collectionDataBaseDao().insertCustomerCollectionProfile(
            CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1
        )

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionCustomerProfiles(
                CollectionTestData.BUSINESS_ID
            ).test().awaitCount(1)

        testObserver.assertValue { it.size == 1 && it[0].customerId == CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1.customerId }
    }

    @Test
    fun ` insertCollectionCustomerProfiless test`() {
        collectionDb.collectionDataBaseDao().insertCustomerCollectionProfiles(
            CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1,
            CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY2
        )

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionCustomerProfiles(
                CollectionTestData.BUSINESS_ID
            ).test().awaitCount(1)

        testObserver.assertValue {
            it.size == 2 && it[0].customerId == CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1.customerId &&
                it[1].customerId == CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY2.customerId
        }
    }

    @Test
    fun ` deleteAllCollectionCustomerProfiles test`() {
        collectionDb.collectionDataBaseDao().insertCustomerCollectionProfiles(
            CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY1,
            CollectionTestData.COLLECTION_CUSTOMER_PROFILE_ENTITY2
        )

        collectionDb.collectionDataBaseDao().deleteAllCollectionCustomerProfiles().test().assertComplete()

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionCustomerProfiles(
                CollectionTestData.BUSINESS_ID
            ).test().awaitCount(1)

        testObserver.assertValue {
            it.isEmpty()
        }
    }

    @Test
    fun `insertCollectionShareInfoItem test`() {
        collectionDb.collectionDataBaseDao().insertCollectionShareInfoItem(
            CollectionTestData.COLLECTION_SHARE_INFO
        )

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionShareInfos(CollectionTestData.BUSINESS_ID).test().awaitCount(1)

        testObserver.assertValue { it.size == 1 && it[0].customer_id == CollectionTestData.COLLECTION_SHARE_INFO.customer_id }
    }

    @Test
    fun `listCollectionShareInfos test`() {
        collectionDb.collectionDataBaseDao().insertCollectionShareInfoItem(
            CollectionTestData.COLLECTION_SHARE_INFO,
            CollectionTestData.COLLECTION_SHARE_INFO2
        )

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionShareInfos(CollectionTestData.BUSINESS_ID).test().awaitCount(1)

        testObserver.assertValue {
            it.size == 2 && it[0].customer_id == CollectionTestData.COLLECTION_SHARE_INFO.customer_id &&
                it[1].customer_id == CollectionTestData.COLLECTION_SHARE_INFO2.customer_id
        }
    }

    @Test
    fun `deleteCollectionShareInfoItem test`() {
        collectionDb.collectionDataBaseDao().insertCollectionShareInfoItem(
            CollectionTestData.COLLECTION_SHARE_INFO,
            CollectionTestData.COLLECTION_SHARE_INFO2
        )

        collectionDb.collectionDataBaseDao()
            .deleteCollectionShareInfoItem(CollectionTestData.COLLECTION_SHARE_INFO2.customer_id)

        val testObserver =
            collectionDb.collectionDataBaseDao().listCollectionShareInfos(CollectionTestData.BUSINESS_ID).test().awaitCount(1)

        testObserver.assertValue {
            it.size == 1 &&
                it[0].customer_id == CollectionTestData.COLLECTION_SHARE_INFO.customer_id
        }
    }
}
