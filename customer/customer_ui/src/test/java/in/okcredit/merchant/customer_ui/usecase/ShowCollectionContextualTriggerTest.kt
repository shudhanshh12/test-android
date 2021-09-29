package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.local.db.CollectionTriggeredCustomers
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.DateTimeUtils

class ShowCollectionContextualTriggerTest {
    private lateinit var showCollectionContextualTrigger: ShowCollectionContextualTrigger

    private val collectionActivationStatus: GetCollectionActivationStatus = mock()
    private val abRepository: AbRepository = mock()
    private val transactionRepo: TransactionRepo = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        val timeInMillis = 100000000L
        val currentDateTime: DateTime = mockk()

        mockkStatic(DateTimeUtils::class)
        every { DateTimeUtils.currentDateTime() } returns currentDateTime

        every { (DateTimeUtils.currentDateTime().millis) } returns (timeInMillis)

        showCollectionContextualTrigger = ShowCollectionContextualTrigger(
            abRepository = { abRepository },
            collectionActivationStatus = { collectionActivationStatus },
            transactionRepo = { transactionRepo },
            firebaseRemoteConfig = { firebaseRemoteConfig },
            customerRepositoryImpl = { customerRepositoryImpl },
            getActiveBusinessId = { getActiveBusinessId }
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
    }

    @Test
    fun `if feature is disabled then return NONE`() {
        runBlocking {
            mockFeature(false)
            mockCollectionActivationStatus(true)
            mockListTransactions(
                listOf(
                    TestData.TRANSACTION1,
                    TestData.TRANSACTION2
                )
            )
            showCollectionContextualTrigger.execute(TestData.CUSTOMER.id).test()
                .assertValue { it.first == CollectionTriggerVariant.NONE }
        }
    }

    @Test
    fun `if empty transaction list then return NONE`() {
        runBlocking {
            mockFeature(true)
            mockCollectionActivationStatus(true)
            mockListTransactions(
                emptyList()
            )
            showCollectionContextualTrigger.execute(TestData.CUSTOMER.id).test()
                .assertValue { it.first == CollectionTriggerVariant.NONE }
        }
    }

    @Test
    fun `if no note added in last transaction return NONE`() {
        runBlocking {
            mockFeature(true)
            mockCollectionActivationStatus(true)
            mockListTransactions(
                listOf(
                    TestData.TRANSACTION1
                )
            )
            showCollectionContextualTrigger.execute(TestData.CUSTOMER.id).test()
                .assertValue { it.first == (CollectionTriggerVariant.NONE) }
        }
    }

    @Test
    fun `if keyword not present in note in last transaction return NONE`() {
        runBlocking {
            mockFeature(true)
            mockCollectionActivationStatus(true)
            mockListTransactions(
                listOf(
                    TestData.TRANSACTION2
                )
            )
            mockFirebase()
            showCollectionContextualTrigger.execute(TestData.CUSTOMER.id).test()
                .assertValue { it.first == (CollectionTriggerVariant.NONE) }
        }
    }

    @Test
    fun `collection not activated keyword present existing customer list empty then return trigger variant`() {
        runBlocking {
            mockFeature(true)
            mockCollectionActivationStatus(false)
            mockListTransactions(
                listOf(TRANSACTION_WITH_GPAY)
            )
            mockFirebase()
            mockCustomerWithCollectionContextualMessage(emptyList())
            showCollectionContextualTrigger.execute(TestData.CUSTOMER.id).test()
                .assertValue { it.first == CollectionTriggerVariant.SETUP_CREDIT_COLLECTION && it.second == "gpay" }
            verify(getActiveBusinessId).execute()
            verify(customerRepositoryImpl).setCustomerWithCollectionContextualMessage(
                TestData.CUSTOMER.id,
                TRANSACTION_WITH_GPAY.id,
                TestData.BUSINESS_ID
            )
            verify(customerRepositoryImpl).setLastContextualTriggerTimestamp(100000000L)
        }
    }

    @Test
    fun `collection activated keyword present existing customer list empty then return trigger variant`() {
        runBlocking {
            mockFeature(true)
            mockCollectionActivationStatus(true)
            mockListTransactions(
                listOf(TRANSACTION_WITH_GPAY)
            )
            mockFirebase()
            mockCustomerWithCollectionContextualMessage(emptyList())
            showCollectionContextualTrigger.execute(TestData.CUSTOMER.id).test()
                .assertValue { it.first == CollectionTriggerVariant.COLLECT_CREDIT_ONLINE && it.second == "gpay" }
            verify(getActiveBusinessId).execute()
            verify(customerRepositoryImpl).setCustomerWithCollectionContextualMessage(
                TestData.CUSTOMER.id,
                TRANSACTION_WITH_GPAY.id,
                TestData.BUSINESS_ID
            )
            verify(customerRepositoryImpl).setLastContextualTriggerTimestamp(100000000L)
        }
    }

    private fun mockFeature(enabled: Boolean) {
        whenever(abRepository.isFeatureEnabled("collection_contextual_trigger")).thenReturn(
            Observable.create {
                it.onNext(enabled)
            }
        )
    }

    private fun mockCollectionActivationStatus(activated: Boolean) {
        whenever(collectionActivationStatus.execute()).thenReturn(
            Observable.create {
                it.onNext(activated)
            }
        )
    }

    private fun mockListTransactions(list: List<Transaction>) {
        whenever(transactionRepo.listTransactions(TestData.CUSTOMER.id, TestData.BUSINESS_ID)).thenReturn(
            Observable.create {
                it.onNext(list)
            }
        )
    }

    private fun mockFirebase(keywords: String = "gpay,g pay,google,phonepe,phone pe,paytm,upi,googlepay") {
        whenever(firebaseRemoteConfig.getString("contextual_note_keywords")).thenReturn(keywords)
    }

    private suspend fun mockCustomerWithCollectionContextualMessage(list: List<CollectionTriggeredCustomers>) {
        whenever(customerRepositoryImpl.getCustomerWithCollectionContextualMessage(TestData.BUSINESS_ID)).thenReturn(list)
    }

    companion object {
        val TRANSACTION_WITH_GPAY = Transaction(
            "xyz",
            Transaction.CREDIT,
            TestData.CUSTOMER.id,
            "",
            13223,
            arrayListOf(),
            "collected from gpay",
            TestData.CURRENT_TIME,
            false,
            false,
            null,
            false,
            DateTime(100000001),
            TestData.CURRENT_TIME,
            true,
            false,
            false,
            "",
            "",
            1,
            Transaction.DEAFULT_CATERGORY,
            true,
            TestData.CURRENT_TIME
        )
    }
}
