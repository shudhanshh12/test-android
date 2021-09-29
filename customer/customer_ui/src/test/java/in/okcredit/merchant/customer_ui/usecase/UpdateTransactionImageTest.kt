package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import org.junit.Before
import org.junit.Test
import java.util.*

class UpdateTransactionImageTest {

    private val transactionRepo: TransactionRepo = mock()
    private val updateTransactionImage = UpdateTransactionImage(transactionRepo)

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `execute() return success`() {
        val requestSelectedImages: ArrayList<merchant.okcredit.accounting.model.TransactionImage> = mock()
        val request = UpdateTransactionImage.Request(requestSelectedImages, "transaction_id")
        whenever(transactionRepo.updateTransactionImage(request.selectedImages, request.transactionId)).thenReturn(
            Completable.complete()
        )

        val testObserver = updateTransactionImage.execute(request).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Success(
                Unit
            )
        )

        verify(transactionRepo).updateTransactionImage(request.selectedImages, request.transactionId)

        testObserver.dispose()
    }

    @Test
    fun `excute() return error`() {
        val mockError = Exception()
        val requestSelectedImages: ArrayList<merchant.okcredit.accounting.model.TransactionImage> = mock()
        val request = UpdateTransactionImage.Request(requestSelectedImages, "transaction_id")
        whenever(transactionRepo.updateTransactionImage(request.selectedImages, request.transactionId)).thenReturn(
            Completable.error(mockError)
        )

        val testObserver = updateTransactionImage.execute(request).test()

        testObserver.assertValues(
            Result.Progress(),
            Result.Failure(
                mockError
            )
        )

        verify(transactionRepo).updateTransactionImage(request.selectedImages, request.transactionId)

        testObserver.dispose()
    }
}
