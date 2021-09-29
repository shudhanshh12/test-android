package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.fileupload.user_migration.domain.repository.MigrationRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.user_migration.contract.models.PredictedData
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RoboflowUploadReceipt @Inject constructor(
    private val migrationRepo: Lazy<MigrationRepo>,
    private val predictedAmount: Lazy<GetPredictedAmount>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(image: CapturedImage): Single<PredictedData> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            migrationRepo.get().uploadImage(image.file.absolutePath, businessId)
                .timeout(10, TimeUnit.SECONDS)
                .flatMap { predictedAmount.get().execute(it) }
        }
    }
}
