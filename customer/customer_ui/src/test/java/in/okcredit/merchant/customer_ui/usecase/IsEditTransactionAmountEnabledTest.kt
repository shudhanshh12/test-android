package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.customer_ui.TestData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class IsEditTransactionAmountEnabledTest {

    private val coreSdk: CoreSdk = mock()
    private val transactionRepo: TransactionRepo = mock()
    private val getCustomer: GetCustomer = mock()
    private val showEditAmountABExperiment: ShowEditAmountABExperiment = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var isEditTransactionAmountEnabled: IsEditTransactionAmountEnabled

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        isEditTransactionAmountEnabled = IsEditTransactionAmountEnabled(
            coreSdk, transactionRepo, getCustomer, showEditAmountABExperiment,
            { getActiveBusinessId }
        )
    }

    @Test
    fun `if core sdk not enabled then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(false))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.TRANSACTION1.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if experiment not enabled then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(false))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.TRANSACTION1.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if transaction collection id is not empty then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(transactionRepo.getTransaction(TestData.ONLINE_TRANSACTION.id, businessId)).thenReturn(Observable.just(TestData.ONLINE_TRANSACTION))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.ONLINE_TRANSACTION.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if transaction is deleted then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(transactionRepo.getTransaction(TestData.DELETED_TRANSACTION.id, businessId)).thenReturn(Observable.just(TestData.DELETED_TRANSACTION))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.DELETED_TRANSACTION.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if transaction is created by customer then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(transactionRepo.getTransaction(TestData.CREATED_BY_CUSTOMER_TRANSACTION.id, businessId)).thenReturn(
            Observable.just(
                TestData.CREATED_BY_CUSTOMER_TRANSACTION
            )
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.CREATED_BY_CUSTOMER_TRANSACTION.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if customer is blocked then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(transactionRepo.getTransaction(TestData.TRANSACTION1.id, businessId)).thenReturn(Observable.just(TestData.TRANSACTION1))
        whenever(getCustomer.execute(TestData.TRANSACTION1.customerId)).thenReturn(
            Observable.just(
                TestData.CUSTOMER.copy(
                    status = Customer.State.BLOCKED.value
                )
            )
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.TRANSACTION1.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if blocked by customer then return false`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(transactionRepo.getTransaction(TestData.TRANSACTION1.id, businessId)).thenReturn(Observable.just(TestData.TRANSACTION1))
        whenever(getCustomer.execute(TestData.TRANSACTION1.customerId)).thenReturn(
            Observable.just(
                TestData.CUSTOMER.copy(
                    blockedByCustomer = true
                )
            )
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.TRANSACTION1.id).test()
        observer.assertValue { !it }
    }

    @Test
    fun `if transaction editable and customer not blocked then return true`() {
        val businessId = "business-id"
        whenever(coreSdk.isCoreSdkFeatureEnabled(businessId)).thenReturn(Single.just(true))
        whenever(showEditAmountABExperiment.execute()).thenReturn(Observable.just(true))
        whenever(transactionRepo.getTransaction(TestData.TRANSACTION1.id, businessId)).thenReturn(Observable.just(TestData.TRANSACTION1))
        whenever(getCustomer.execute(TestData.TRANSACTION1.customerId)).thenReturn(Observable.just(TestData.CUSTOMER))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val observer = isEditTransactionAmountEnabled.execute(TestData.TRANSACTION1.id).test()
        observer.assertValue { it }
    }
}
