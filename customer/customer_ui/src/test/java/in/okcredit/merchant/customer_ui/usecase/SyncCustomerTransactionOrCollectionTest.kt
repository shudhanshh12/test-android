package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class SyncCustomerTransactionOrCollectionTest {

    private lateinit var syncCustomerTransactionOrCollection: SyncCustomerTransactionOrCollection

    private val collectionRepository: CollectionRepository = mockk()
    private val transactionRepo: TransactionRepo = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        syncCustomerTransactionOrCollection = SyncCustomerTransactionOrCollection(
            collectionRepository = { collectionRepository },
            transactionRepo = { transactionRepo },
            getActiveBusinessId = { getActiveBusinessId },
        )

        every { getActiveBusinessId.execute() } returns Single.just(TestData.BUSINESS_ID)
    }

    @Test
    fun `return none when transaction and collection are empty`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create { it.onNext(emptyList()) }

        every {
            transactionRepo.listOnlineTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create { it.onNext(emptyList()) }

        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.NONE }
            dispose()
        }
    }

    @Test
    fun `return Transaction sync when transaction is empty and collection are non empty`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create { it.onNext(listOf(TestData.COLLECTION)) }

        every {
            transactionRepo.listOnlineTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create { it.onNext(emptyList()) }

        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.TRANSACTION }
            dispose()
        }
    }

    @Test
    fun `return collection sync when transaction is non empty and collection is empty`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(emptyList())
        }

        every {
            transactionRepo.listOnlineTransactions(
                TestData.CUSTOMER.id,
                TestData.BUSINESS_ID
            )
        } returns Observable.create {
            it.onNext(listOf(TestData.ONLINE_TRANSACTION))
        }

        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.COLLECTION }
            dispose()
        }
    }

    @Test
    fun `return collection sync when transaction is non empty and collection is non empty but collection size is less`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.COLLECTION))
        }

        every {
            transactionRepo.listOnlineTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.ONLINE_TRANSACTION, TestData.TRANSACTION1))
        }

        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.COLLECTION }
            dispose()
        }
    }

    @Test
    fun `return transaction sync when transaction is non empty and collection is non empty but transaction size is less`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.COLLECTION, TestData.COLLECTION_2))
        }

        every {
            transactionRepo.listOnlineTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.ONLINE_TRANSACTION))
        }
        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.TRANSACTION }
            dispose()
        }
    }

    @Test
    fun `return BOTH sync when at least one collection is present with status 2`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.COLLECTION.copy(status = 2), TestData.COLLECTION_2.copy(status = 5)))
        }

        every {
            transactionRepo.listOnlineTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.ONLINE_TRANSACTION, TestData.ONLINE_TRANSACTION))
        }
        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.BOTH }
            dispose()
        }
    }

    @Test
    fun `return none when size is equal and no collection present in PAID status`() {
        every {
            collectionRepository.getCollectionsOfCustomerOrSupplier(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.COLLECTION.copy(status = 5), TestData.COLLECTION_2.copy(status = 5)))
        }

        every {
            transactionRepo.listOnlineTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)
        } returns Observable.create {
            it.onNext(listOf(TestData.ONLINE_TRANSACTION, TestData.ONLINE_TRANSACTION))
        }
        syncCustomerTransactionOrCollection.execute(TestData.CUSTOMER.id).test().apply {
            assertValue { it == SyncCustomerTransactionOrCollection.SyncData.NONE }
            dispose()
        }
    }
}
