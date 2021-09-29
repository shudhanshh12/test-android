package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
import tech.okcredit.android.base.utils.DateTimeUtils

class ShowCustomerPaymentIntentTriggerTest {

    private lateinit var showCustomerPaymentIntentTrigger: ShowCustomerPaymentIntentTrigger

    private val collectionRepository: CollectionRepository = mock()
    private val transactionRepo: TransactionRepo = mock()
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

        showCustomerPaymentIntentTrigger = ShowCustomerPaymentIntentTrigger(
            collectionRepository = { collectionRepository },
            transactionRepo = { transactionRepo },
            getActiveBusinessId = { getActiveBusinessId },
            customerRepositoryImpl = { customerRepositoryImpl }
        )
    }

    @Test
    fun `if collection activated return false`() {
        runBlocking {
            mockCollectionActivationStatus(true)
            mockListTransactions(
                listOf(
                    TestData.TRANSACTION1,
                    TestData.TRANSACTION2
                )
            )
            mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE.copy(paymentIntent = true))
            mockBusinessId()
            showCustomerPaymentIntentTrigger.execute(TestData.CUSTOMER.id).test().assertValue { !it }
        }
    }

    @Test
    fun `if payment is false return false`() {
        runBlocking {
            mockCollectionActivationStatus(false)
            mockListTransactions(
                listOf(
                    TestData.TRANSACTION1,
                    TestData.TRANSACTION2
                )
            )
            mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE.copy(paymentIntent = false))
            mockTxnCountForPaymentIntentEnabled(1)
            mockBusinessId()
            showCustomerPaymentIntentTrigger.execute(TestData.CUSTOMER.id).test().assertValue { !it }
        }
    }

    @Test
    fun `if payment intent is true and current txn count is zero return true`() {
        runBlocking {
            mockCollectionActivationStatus(false)
            mockListTransactions(
                listOf(
                    TestData.TRANSACTION1,
                    TestData.TRANSACTION2
                )
            )
            mockCustomerCollectionProfile(TestData.CUSTOMER_COLLECTION_PROFILE.copy(paymentIntent = true))
            mockTxnCountForPaymentIntentEnabled(0)
            mockBusinessId()
            showCustomerPaymentIntentTrigger.execute(TestData.CUSTOMER.id).test().assertValue { it }
            verify(customerRepositoryImpl).setTxnCountForPaymentIntent(TestData.CUSTOMER.id, 2, TestData.BUSINESS_ID)
        }
    }

    private fun mockBusinessId() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
    }

    private fun mockCollectionActivationStatus(activated: Boolean) {
        whenever(collectionRepository.isCollectionActivated()).thenReturn(
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

    private fun mockCustomerCollectionProfile(profile: CollectionCustomerProfile) {
        whenever(collectionRepository.getCollectionCustomerProfile(TestData.CUSTOMER.id, TestData.BUSINESS_ID)).thenReturn(
            Observable.create {
                it.onNext(profile)
            }
        )
    }

    private suspend fun mockTxnCountForPaymentIntentEnabled(count: Int) {
        whenever(customerRepositoryImpl.getTxnCountForPaymentIntentEnabled(TestData.CUSTOMER.id)).thenReturn(count)
    }
}
