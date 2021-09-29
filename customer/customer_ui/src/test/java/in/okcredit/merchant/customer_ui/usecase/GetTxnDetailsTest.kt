package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
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

class GetTxnDetailsTest {
    private val transactionRepo: TransactionRepo = mock()
    private val getCustomer: GetCustomer = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getTxnDetails = GetTxnDetails(transactionRepo, getCustomer, { getActiveBusinessId })

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `getTxnDetails() return success`() {

        val responseTransaction = TestData.TRANSACTION1
        val responseCustomer = TestData.CUSTOMER
        val businessId = "business-id"

        whenever(transactionRepo.getTransaction("req", businessId)).thenReturn(Observable.just(responseTransaction))
        whenever(getCustomer.execute(responseTransaction.customerId)).thenReturn(Observable.just(responseCustomer))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = getTxnDetails.execute("req").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(
                GetTxnDetails.Response(responseTransaction, responseCustomer)
            )
        )
        verify(transactionRepo).getTransaction("req", businessId)
        verify(getCustomer).execute(responseTransaction.customerId)

        testObserver.dispose()
    }

    @Test
    fun `getTxnDetails() getTransaction fails so return error`() {

        val mockError = Exception("Some error in Transaction")
        val businessId = "business-id"

        whenever(transactionRepo.getTransaction("req", businessId)).thenReturn(Observable.error(mockError))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = getTxnDetails.execute("req").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(
                mockError
            )
        )
        verify(transactionRepo).getTransaction("req", businessId)

        testObserver.dispose()
    }

    @Test
    fun `getTxnDetails() getCustomer fails so return error`() {

        val mockError = Exception("Some error in Transaction")
        val businessId = "business-id"

        val responseTransaction = TestData.TRANSACTION1

        whenever(transactionRepo.getTransaction("req", businessId)).thenReturn(Observable.just(responseTransaction))
        whenever(getCustomer.execute(responseTransaction.customerId)).thenReturn(Observable.error(mockError))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = getTxnDetails.execute("req").test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(
                mockError
            )
        )
        verify(transactionRepo).getTransaction("req", businessId)

        testObserver.dispose()
    }
}
