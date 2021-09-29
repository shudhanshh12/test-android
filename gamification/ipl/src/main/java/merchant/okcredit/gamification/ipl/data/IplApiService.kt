package merchant.okcredit.gamification.ipl.data

import merchant.okcredit.gamification.ipl.game.data.server.model.request.*
import merchant.okcredit.gamification.ipl.game.data.server.model.response.*
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IplApiService {

    @GET("v1.0/ipl2021/activeMatches")
    suspend fun getActiveMatches(): ActiveMatches

    @POST("v1.0/ipl2021/onboardingDetails")
    suspend fun getOnboardingDetails(@Body onboardingRequest: OnboardingRequest): OnboardingDetails

    @POST("v1.0/ipl2021/getSundayGame")
    suspend fun getSundayGameDetails(@Body merchantRequest: MerchantRequest): SundayGameResponse

    @POST("v2.0/ipl2021/getLeaderboard")
    suspend fun getLeaderBoardDetails(@Body leaderBoardRequest: LeaderBoardRequest): LeaderBoardResponse

    @POST("v1.0/ipl2021/getBoosterQuestion")
    suspend fun getBoosterQuestion(@Body onboardingRequest: OnboardingRequest): BoosterQuestion

    @POST("v1.0/ipl2021/teamChoice")
    suspend fun selectTeam(@Body choiceRequest: ChoiceRequest): OnboardingDetails

    @POST("v1.0/ipl2021/batsmanChoice")
    suspend fun selectBatsman(@Body choiceRequest: ChoiceRequest): OnboardingDetails

    @POST("v1.0/ipl2021/bowlerChoice")
    suspend fun selectBowler(@Body choiceRequest: ChoiceRequest): OnboardingDetails

    @POST("v1.0/ipl2021/ListMysteryPrizes")
    suspend fun getMysteryPrizes(@Body merchantRequest: MerchantRequest): GetMysteryCardsResponse

    @POST("v1.0/ipl2021/ClaimMysteryPrize")
    suspend fun claimMysteryPrize(@Body claimMysteryPrizeRequest: ClaimMysteryPrizeRequest): ResponseBody

    @POST("v1.0/ipl2021/submitBoosterChoice")
    suspend fun submitBoosterChoice(@Body submitBoosterRequest: SubmitBoosterRequest): OnboardingDetails

    @POST("v1.0/ipl2021/getPrediction")
    suspend fun getPrediction(@Body matchRequest: MatchRequest): PredictionResponse
}
