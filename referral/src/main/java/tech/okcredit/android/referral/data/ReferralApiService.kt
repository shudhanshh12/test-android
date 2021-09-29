package tech.okcredit.android.referral.data

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ReferralInfo
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import tech.okcredit.android.referral.ui.rewards_on_signup.model.GetReferralTargetsApiRequest
import tech.okcredit.android.referral.ui.rewards_on_signup.model.GetReferralTargetsApiResponse

interface ReferralApiService {

    @POST("v1/referral/ListReferredMerchants")
    fun getReferredMerchants(
        @Body request: GetReferredMerchantsRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<GetReferredMerchantsResponse>

    @POST("v1/referral/nudgeUser")
    fun notifyUser(
        @Body request: NotifyMerchantRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<NotifyMerchantResponse>

    @POST("v1.1/referral/checkJourneyQualification")
    fun checkJourneyQualification(
        @Body request: JourneyQualificationRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<JourneyQualificationResponse>

    @GET("v1/referral/status/{merchant_id}")
    fun getProfile(
        @Path("merchant_id") merchantId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<ReferralInfo>

    @GET("v1.1/referral/targetedUsers")
    suspend fun getTargetedUsers(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ReferralApiMessages.GetTargetedUsersResponse

    @GET("v1/referral/shareContent/{target_user_id}")
    suspend fun getShareContent(
        @Path("target_user_id") targetUserId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ReferralApiMessages.GetShareContentResponse

    // get referral link
    @GET("v1/referral/link")
    fun getReferralLink(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<GetReferralLinkResponse>>

    @POST("v1/referral/nextTargets")
    suspend fun getReferralTarget(
        @Body request: GetReferralTargetsApiRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): GetReferralTargetsApiResponse
}
