package `in`.okcredit.business_health_dashboard.datasource.remote.apiClient

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface BusinessHealthApiClient {
    @GET("merchant/dashboard")
    fun getDashboardData(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<BusinessHealthDashboardModelDto>>

    @POST("insight/feedback")
    fun submitFeedbackForTrend(
        @Body request: TrendFeedbackRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable
}
