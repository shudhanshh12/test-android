package `in`.okcredit.onboarding.marketing

import retrofit2.http.Body
import retrofit2.http.POST

interface MarketingApiService {

    @POST("v1/analytics/marketing_data")
    suspend fun reportMarketingData(@Body req: MarketingData)
}
