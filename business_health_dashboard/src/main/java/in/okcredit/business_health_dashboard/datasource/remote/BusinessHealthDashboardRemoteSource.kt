package `in`.okcredit.business_health_dashboard.datasource.remote

import `in`.okcredit.business_health_dashboard.datasource.remote.apiClient.BusinessHealthApiClient
import `in`.okcredit.business_health_dashboard.datasource.remote.apiClient.BusinessHealthDashboardModelDto
import `in`.okcredit.business_health_dashboard.datasource.remote.apiClient.TrendFeedbackRequest
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

@Reusable
class BusinessHealthDashboardRemoteSource @Inject constructor(
    private val businessHealthApiClient: Lazy<BusinessHealthApiClient>,
) {
    fun getBusinessHealthDashboardDataDto(businessId: String): Single<BusinessHealthDashboardModelDto> {
        return businessHealthApiClient.get().getDashboardData(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { response ->
                if (response.isSuccessful && response.body() != null) {
                    return@map response.body()
                } else {
                    throw response.asError()
                }
            }
    }

    fun submitFeedbackForTrend(trendId: String, response: String, businessId: String): Completable {
        return businessHealthApiClient.get().submitFeedbackForTrend(
            TrendFeedbackRequest(insightId = trendId, response = response),
            businessId
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
    }
}
