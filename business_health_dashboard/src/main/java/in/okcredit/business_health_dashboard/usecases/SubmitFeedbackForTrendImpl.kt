package `in`.okcredit.business_health_dashboard.usecases

import `in`.okcredit.business_health_dashboard.contract.model.usecases.SubmitFeedbackForTrend
import `in`.okcredit.business_health_dashboard.repository.BusinessHealthDashboardRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SubmitFeedbackForTrendImpl @Inject constructor(
    private val businessHealthDashboardRepository: Lazy<BusinessHealthDashboardRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SubmitFeedbackForTrend {

    override val POSITIVE_FEEDBACK_RESPONSE_STRING: String = "YES"
    override val NEGATIVE_FEEDBACK_RESPONSE_STRING: String = "NO"

    override fun execute(trendId: String, response: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            businessHealthDashboardRepository.get().submitFeedbackForTrend(trendId, response, businessId)
                .andThen(businessHealthDashboardRepository.get().fetchFromRemoteAndSaveToLocal(businessId))
        }
    }
}
