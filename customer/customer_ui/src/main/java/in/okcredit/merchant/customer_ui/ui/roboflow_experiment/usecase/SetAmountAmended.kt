package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker
import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.analytics.RoboflowEventTracker.Companion.STARTED
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.user_migration.contract.UserMigrationRepository
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest
import javax.inject.Inject

class SetAmountAmended @Inject constructor(
    private val migrationRepository: Lazy<UserMigrationRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val tracker: Lazy<RoboflowEventTracker>
) {
    fun execute(
        customerId: String,
        transactionId: String = "",
        newAmount: Long
    ): Completable {

        kotlin.runCatching { tracker.get().trackSetAmountAmended(STARTED) }

        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            migrationRepository.get().getFileObjectIdFromPredictedData(businessId).firstOrError()
                .flatMapCompletable { fileUploadId ->
                    val request = SetAmountAmendedApiRequest(
                        merchantId = businessId,
                        customerAccountId = customerId,
                        fileUploadId = fileUploadId,
                        transactionId = transactionId,
                        newAmount = newAmount
                    )
                    migrationRepository.get().setAmountAmend(request, businessId)
                        .andThen(migrationRepository.get().clearPredictionData(businessId))
                }
        }
    }
}
