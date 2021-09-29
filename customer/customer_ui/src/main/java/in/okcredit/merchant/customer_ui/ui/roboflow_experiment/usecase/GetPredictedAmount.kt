package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.user_migration.contract.UserMigrationRepository
import tech.okcredit.user_migration.contract.models.PredictedData
import javax.inject.Inject

class GetPredictedAmount @Inject constructor(
    private val migrationRepo: Lazy<UserMigrationRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>
) {

    fun execute(imageUrl: String): Single<PredictedData> {
        return rxSingle {
            val businessId = getActiveBusinessId.get().execute().await()
            migrationRepo.get().getPredictedData(imageUrl, businessId)
        }
    }
}
