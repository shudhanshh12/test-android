package `in`.okcredit.merchant.customer_ui.usecase.pre_network_onboarding

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CanShowPreNetworkOnboardingBanner @Inject constructor(
    private val onboardingRepo: Lazy<OnboardingRepo>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    companion object {
        const val EXPERIMENT_NAME = "activation_android-all-prenetwork_onboarding"
        const val VARIANT_NAME = "prenetwork"
    }

    suspend fun execute(customerId: String) = withContext(Dispatchers.IO) {
        val businessId = getActiveBusinessId.get().execute().await()
        val latestTransactionCreateTime = transactionRepo.get()
            .getLatestTransactionCustomer(customerId, businessId).await().createdAt

        val isInActiveTransaction = checkTransactionsAreInActiveMoreThan30Days(latestTransactionCreateTime)

        val isPreNetworkRelationShip = onboardingRepo.get().getPreNetworkRelationships()
            .find { it == customerId }.isNotNullOrBlank()

        val latestTransactionCreateTimeInDays = getDaysFromDateTime(latestTransactionCreateTime)

        return@withContext (
            isUserFallInExperiment() &&
                isPreNetworkRelationShip &&
                isInActiveTransaction
            ) to latestTransactionCreateTimeInDays
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
