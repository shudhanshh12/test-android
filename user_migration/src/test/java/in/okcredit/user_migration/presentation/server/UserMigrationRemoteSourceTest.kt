package `in`.okcredit.user_migration.presentation.server

import `in`.okcredit.user_migration.presentation.UserMigrationRepositoryImplTest.Companion.fakePredictedData
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.user_migration.contract.models.GetPredictedDataApiRequest
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest

class UserMigrationRemoteSourceTest {
    private val mockApiService: UserMigrationApiClient = mock()
    private val schedulerProvider: SchedulerProvider = mock()

    private val userMigrationRemoteSource = UserMigrationRemoteSource { mockApiService }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `should return predicted data from api`() {
        runBlocking {
            val fakeImageUrl = "imageUrl"
            val businessId = "business-id"
            val fakeGetPredictedDataApiRequest = GetPredictedDataApiRequest(
                imageUrl = fakeImageUrl,
                merchantId = businessId
            )
            whenever(mockApiService.getPredictedData(fakeGetPredictedDataApiRequest, businessId))
                .thenReturn(fakePredictedData)

            val result = userMigrationRemoteSource.getPredictedDataFromRemote(fakeImageUrl, businessId)

            Truth.assertThat(result).isEqualTo(fakePredictedData)
        }
    }

    @Test
    fun `setPredictedAmountAmended should completed`() {
        runBlocking {
            val businessId = "business-id"
            val fakeRequest = SetAmountAmendedApiRequest(
                merchantId = businessId,
                customerAccountId = "23",
                fileUploadId = "4",
                transactionId = "22",
                newAmount = 123L
            )
            whenever(mockApiService.setPredictedAmountAmended(fakeRequest, businessId))
                .thenReturn(Completable.complete())

            val testObserver = userMigrationRemoteSource.setAmountAmended(fakeRequest, businessId).test()

            testObserver.assertComplete()
        }
    }
}
