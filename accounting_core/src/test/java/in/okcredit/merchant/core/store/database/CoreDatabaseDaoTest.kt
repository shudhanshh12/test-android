package `in`.okcredit.merchant.core.store.database

import `in`.okcredit.merchant.core.TestData
import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tech.okcredit.android.base.extensions.isNotNullOrBlank

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CoreDatabaseTest {

    private lateinit var coreDatabase: CoreDatabase

    @Before
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        coreDatabase = Room.inMemoryDatabaseBuilder(
            context,
            CoreDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        coreDatabase.close()
    }

    @Test
    fun `put customer test`() {
        val completable = coreDatabase.coreDatabaseDao().putCustomer(TestData.CUSTOMER_DB_1).test()
        completable.assertComplete()

        val testObserver = coreDatabase.coreDatabaseDao().getCustomer(TestData.CUSTOMER_DB_1.id).test().awaitCount(1)
        testObserver.assertValue { it.id == TestData.CUSTOMER_DB_1.id }
    }

    @Test
    fun `put multiple customer test`() {
        coreDatabase.coreDatabaseDao()
            .putCustomers(listOf(TestData.CUSTOMER_DB_1, TestData.CUSTOMER_DB_2, TestData.CUSTOMER_DB_3))

        val testObserver = coreDatabase.coreDatabaseDao().getCustomerCount(TestData.BUSINESS_ID).test().awaitCount(1)
        testObserver.assertValue { it == 3 }
    }

    @Test
    fun `clear customer for business test`() {
        coreDatabase.coreDatabaseDao()
            .putCustomers(listOf(TestData.CUSTOMER_DB_1, TestData.CUSTOMER_DB_2, TestData.CUSTOMER_DB_3))

        val testObserver = coreDatabase.coreDatabaseDao().clearCustomerTable(TestData.BUSINESS_ID).andThen(
            coreDatabase.coreDatabaseDao().getCustomerCount(TestData.BUSINESS_ID)
        ).test().awaitCount(1)

        testObserver.assertValue(0)
    }

    @Test
    fun `delete customer test`() {
        coreDatabase.coreDatabaseDao()
            .putCustomers(listOf(TestData.CUSTOMER_DB_1, TestData.CUSTOMER_DB_2, TestData.CUSTOMER_DB_3))

        val testObserver = coreDatabase.coreDatabaseDao().deleteCustomerTable().andThen(
            coreDatabase.coreDatabaseDao().getCustomerCount(TestData.BUSINESS_ID)
        ).test().awaitCount(1)

        testObserver.assertValue(0)
    }

    @Test
    fun `is customer present test`() {
        coreDatabase.coreDatabaseDao().putCustomers(listOf(TestData.CUSTOMER_DB_1))

        val testObserver =
            coreDatabase.coreDatabaseDao().isCustomerPresent(TestData.CUSTOMER_DB_1.id).test().awaitCount(1)
        testObserver.assertValue { it == 1 }
    }

    @Test
    fun `insert transaction test`() {
        coreDatabase.coreDatabaseDao().insertTransaction(
            TestData.TRANSACTION_DB_1,
            TestData.TRANSACTION_DB_2,
            TestData.TRANSACTION_DB_3,
            TestData.TRANSACTION_DB_4
        )

        val testObserver = coreDatabase.coreDatabaseDao().getAllTransactionsCount(TestData.BUSINESS_ID).test()
        println(testObserver.values())
        testObserver.assertValue { it == 4 }
    }

    @Test
    fun `getDefaulters test`() {
        coreDatabase.coreDatabaseDao().putCustomer(TestData.CUSTOMER_DB_1).test().assertComplete()
        coreDatabase.coreDatabaseDao().putCustomer(TestData.CUSTOMER_DB_2).test().assertComplete()
        coreDatabase.coreDatabaseDao().putCustomer(TestData.CUSTOMER_DB_3).test().assertComplete()

        coreDatabase.coreDatabaseDao().insertTransaction(
            TestData.TRANSACTION_DB_1,
            TestData.TRANSACTION_DB_2,
            TestData.TRANSACTION_DB_3,
            TestData.TRANSACTION_DB_4
        )

        val test = coreDatabase.coreDatabaseDao().getDefaulters(TestData.BUSINESS_ID).test().awaitCount(1)
        test.values().last().forEach {
            assert(it.status == 1) // customer should be active
            assert(it.mobile.isNotNullOrBlank()) // mobile should not be null
            assert(it.balance < -1000) // balance due should be greater than 10 rs
            assert(it.balance > -10000000) // balance due should be less than 1 lac
        }
    }
}
