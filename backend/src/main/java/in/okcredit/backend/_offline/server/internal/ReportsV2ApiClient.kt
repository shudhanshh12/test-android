package `in`.okcredit.backend._offline.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ReportsV2ApiClient {

    @POST("report")
    fun generateReportUrl(
        @Body generateReportUrlRequest: GenerateReportUrlRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<GenerateReportUrlResponse>

    @GET("report")
    fun getReportUrl(
        @Query("report-id") reportId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<GetReportUrlResponse>
}
