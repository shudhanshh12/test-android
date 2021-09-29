package `in`.okcredit.merchant.rewards.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RewardsApiClient {

    @POST("v2/reward/list")
    suspend fun listRewards(
        @Body req: ListRewardApiRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ListRewardsApiResponse

    @POST("v2/reward/claim")
    suspend fun claimReward(
        @Body req: ClaimRewardRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ClaimRewardResponse
}
