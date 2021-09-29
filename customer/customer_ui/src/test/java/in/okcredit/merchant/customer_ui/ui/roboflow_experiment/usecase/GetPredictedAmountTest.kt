package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.await
import org.junit.Test
import tech.okcredit.user_migration.contract.UserMigrationRepository
import tech.okcredit.user_migration.contract.models.AmountBox
import tech.okcredit.user_migration.contract.models.PredictedData

class GetPredictedAmountTest {
    private val mockMigrationRepo: UserMigrationRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getPredictedAmount = GetPredictedAmount(
        { mockMigrationRepo },
        { getActiveBusinessId }
    )

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

    @Test
    fun `assert useCase should return fake predictedData`() {
        runBlocking {
            val fakeImagePath = "/0/user/storage/internal/xyz.jpeg"
            val fakeMerchantId = "1234"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(fakeMerchantId))
            whenever(mockMigrationRepo.getPredictedData(fakeImagePath, fakeMerchantId)).thenReturn(fakePredictedData)

            val result = getPredictedAmount.execute(fakeImagePath).await()

            Truth.assertThat(result).isEqualTo(fakePredictedData)
        }
    }
}
