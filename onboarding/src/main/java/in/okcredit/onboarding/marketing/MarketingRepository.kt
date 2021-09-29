package `in`.okcredit.onboarding.marketing

import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class MarketingRepository @Inject constructor(
    private val marketingApiService: Lazy<MarketingApiService>,
) {

    suspend fun reportMarketingData(
        aaid: String,
        isSignup: Boolean,
        loginTime: Long,
        mediaSource: String,
        campaign: String,
    ) {
        val appsflyer = Appsflyer(mediaSource, campaign)
        val marketingData = MarketingData(aaid, isSignup, loginTime, appsflyer)

        marketingApiService.get().reportMarketingData(marketingData)
    }
}
