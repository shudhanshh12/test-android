package `in`.okcredit.user_migration.presentation

import `in`.okcredit.user_migration.presentation.server.UserMigrationLocalSource
import `in`.okcredit.user_migration.presentation.server.UserMigrationRemoteSource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.user_migration.contract.models.AmountBox
import tech.okcredit.user_migration.contract.models.PredictedData
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest

class UserMigrationRepositoryImplTest {
    private val mockRemoteSource: UserMigrationRemoteSource = mock()
    private val mockLocalSource: UserMigrationLocalSource = mock()

    private val userMigrationRepositoryImpl = UserMigrationRepositoryImpl(
        remoteSource = { mockRemoteSource },
        localSource = { mockLocalSource }
    )

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `should call getPredictedData from localSource`() {
        runBlocking {
            val businessId = "business-id"
            val fakeImageUrl = "baba ka dhaba image"
            whenever(
                mockRemoteSource.getPredictedDataFromRemote(
                    fakeImageUrl,
                    businessId
                )
            ).thenReturn(fakePredictedData)

            userMigrationRepositoryImpl.getPredictedData(fakeImageUrl, businessId)
            verify(mockRemoteSource).getPredictedDataFromRemote(fakeImageUrl, businessId)
            verify(mockLocalSource).setPredictedData(fakePredictedData, businessId)
        }
    }

    @Test
    fun `should call getPredictedDataFromLocalSource from local source`() {
        runBlocking {
            val businessId = "business-id"
            userMigrationRepositoryImpl.getPredictedDataFromLocalSource(businessId)

            verify(mockLocalSource).getPredictedData(businessId)
        }
    }

    @Test
    fun `should call setAmountAmended from remote source`() {
        runBlocking {
            val businessId = "business-id"
            val fakeRequest = SetAmountAmendedApiRequest(
                merchantId = businessId,
                customerAccountId = "23",
                fileUploadId = "4",
                transactionId = "22",
                newAmount = 123L
            )
            userMigrationRepositoryImpl.setAmountAmended(fakeRequest, businessId)

            verify(mockRemoteSource).setAmountAmended(fakeRequest, businessId)
        }
    }

    @Test
    fun `should call clearPredictionData from local source`() {
        runBlocking {
            val businessId = "business-id"
            userMigrationRepositoryImpl.clearPredictionData(businessId)

            verify(mockLocalSource).clearPredictedData(businessId)
        }
    }

    companion object {
        val fakeAmountBox = AmountBox(
            amount = 25000L,
            boxCoordinateX1 = 0,
            boxCoordinateX2 = 0,
            boxCoordinateY1 = 0,
            boxCoordinateY2 = 0
        )

        val fakePredictedData = PredictedData(
            fakeAmountBox,
            fileName = "",
            fileObjectId = ""
        )
    }
}
