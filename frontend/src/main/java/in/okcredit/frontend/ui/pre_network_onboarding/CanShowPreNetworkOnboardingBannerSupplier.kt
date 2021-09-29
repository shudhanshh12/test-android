package `in`.okcredit.frontend.ui.pre_network_onboarding

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.onboarding.contract.OnboardingRepo
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Days
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CanShowPreNetworkOnboardingBannerSupplier @Inject constructor(
    private val onboardingRepo: Lazy<OnboardingRepo>,
    private val supplierTransactionRepo: Lazy<SupplierCreditRepository>,
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    companion object {
        const val EXPERIMENT_NAME = "activation_android-all-prenetwork_onboarding"
        const val VARIANT_NAME = "prenetwork"
    }

    suspend fun execute(supplierId: String) = withContext(Dispatchers.IO) {
        val businessId = getActiveBusinessId.get().execute().await()

        val latestTransactionCreateTime = supplierTransactionRepo.get()
            .getLatestTransactionCreateTimeOnSupplier(supplierId, businessId)

        val isInActiveTransaction = checkTransactionsAreInActiveMoreThan30Days(latestTransactionCreateTime)

        val isPreNetworkRelationship = onboardingRepo.get().getPreNetworkRelationships()
            .find { it == supplierId }
            .isNotNullOrBlank()

        val latestTransactionTimeInDays = getDaysFromDateTime(latestTransactionCreateTime)

        return@withContext (
            isUserFallInExperiment() &&
                isPreNetworkRelationship &&
                isInActiveTransaction
            ) to latestTransactionTimeInDays
    }

    private suspend fun checkTransactionsAreInActiveMoreThan30Days(latestTransactionCreateTime: DateTime) =
        withContext(Dispatchers.IO) {
            return@withContext (
                System.currentTimeMillis() -
                    latestTransactionCreateTime.millis
                ) > TimeUnit.DAYS.toMillis(30)
        }

    private suspend fun isUserFallInExperiment(): Boolean {
        return Single.zip(
            ab.get().isExperimentEnabled(EXPERIMENT_NAME).firstOrError(),
            ab.get().getExperimentVariant(EXPERIMENT_NAME).firstOrError(),
            { experimentEnabled, experimentVariant ->
                experimentEnabled && experimentVariant.equals(VARIANT_NAME, true)
            }
        ).await()
    }

    private fun getDaysFromDateTime(dateTime: DateTime): String {
        val currentDate = DateTimeUtils.currentDateTime()
        return Days.daysBetween(currentDate, dateTime).days.toString()
    }
}
