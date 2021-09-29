package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.user_migration.contract.UserMigrationRepository
import tech.okcredit.user_migration.contract.models.AmountBox
import tech.okcredit.user_migration.contract.models.PredictedData

class SetAmountAmendedTest {
    private val mockMigrationRepository: UserMigrationRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val mockTracker: RoboflowEventTracker = mock()
    lateinit var testScheduler: TestScheduler
    private val mockSchedulerProvider: SchedulerProvider = mock()

    private val setAmountAmended = SetAmountAmended(
        { mockMigrationRepository },
        { getActiveBusinessId },
        { mockTracker }
    )

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        testScheduler = TestScheduler()
        whenever(mockSchedulerProvider.io()).thenReturn(Schedulers.trampoline())
        every { ThreadUtils.computation() } returns Schedulers.trampoline()
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

//    @Test
//    fun `Should return Completable Complete when setAmountAmended is completed`() {
//        runBlocking {
//            val fakeMerchantId = "12345"
//            val fakeCustomerId = "1234"
//            val fakeTransactionId = "123"
//            val fakeNewAmount = 123L
//
//            val fakeRequest = SetAmountAmendedApiRequest(
//                merchantId = fakeMerchantId,
//                customerAccountId = fakeCustomerId,
//                fileUploadId = fakePredictedData.fileObjectId,
//                transactionId = fakeTransactionId,
//                newAmount = fakeNewAmount
//            )
//
//
//            whenever(mockMerchantRepository.getMerchantIdCoroutine()).thenReturn(flowOf(fakeMerchantId))
//            whenever(mockMigrationRepository.getPredictedDataFromLocalSource()).thenReturn(fakePredictedData)
//            whenever(mockMigrationRepository.setAmountAmended(fakeRequest)).thenReturn(
//                Completable.complete()
//            )
//
//            val result = setAmountAmended.execute(fakeCustomerId, fakeTransactionId, fakeNewAmount).test()
//
//            testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS)
//
//            result.assertComplete()
//        }
//
//    }

    companion object {

        private val fakeAmountBox = AmountBox(
            amount = 25000L,
            boxCoordinateX1 = 0,
            boxCoordinateX2 = 0,
            boxCoordinateY1 = 0,
            boxCoordinateY2 = 0
        )

        private val fakePredictedData = PredictedData(
            fakeAmountBox,
            width = 100,
            height = 200,
            fileName = "",
            fileObjectId = ""
        )
    }
}
