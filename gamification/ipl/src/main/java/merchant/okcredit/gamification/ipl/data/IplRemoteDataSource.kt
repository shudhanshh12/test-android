package merchant.okcredit.gamification.ipl.data

import dagger.Lazy
import merchant.okcredit.gamification.ipl.game.data.server.model.request.*
import okhttp3.ResponseBody
import javax.inject.Inject

class IplRemoteDataSource @Inject constructor(
    private val iplApiService: Lazy<IplApiService>,
) {

    suspend fun getActivesMatches() = iplApiService.get().getActiveMatches()

    suspend fun getOnboarding(onboardingRequest: OnboardingRequest) =
        iplApiService.get().getOnboardingDetails(onboardingRequest)

    suspend fun getSundayGameDetails(merchantRequest: MerchantRequest) =
        iplApiService.get().getSundayGameDetails(merchantRequest)

    suspend fun getLeaderBoardDetails(leaderBoardRequest: LeaderBoardRequest) =
        iplApiService.get().getLeaderBoardDetails(leaderBoardRequest)

    suspend fun getBoosterQuestion(onboardingRequest: OnboardingRequest) =
        iplApiService.get().getBoosterQuestion(onboardingRequest)

    suspend fun selectTeam(choiceRequest: ChoiceRequest) =
        iplApiService.get().selectTeam(choiceRequest)

    suspend fun selectBatsman(choiceRequest: ChoiceRequest) = iplApiService.get().selectBatsman(choiceRequest)

    suspend fun selectBowler(choiceRequest: ChoiceRequest) = iplApiService.get().selectBowler(choiceRequest)

    suspend fun submitBoosterChoice(choiceRequest: SubmitBoosterRequest) =
        iplApiService.get().submitBoosterChoice(choiceRequest)

    suspend fun getMysteryPrizes(merchantRequest: MerchantRequest) =
        iplApiService.get().getMysteryPrizes(merchantRequest).prizes

    suspend fun claimMysteryPrize(claimMysteryPrizeRequest: ClaimMysteryPrizeRequest): ResponseBody =
        iplApiService.get().claimMysteryPrize(claimMysteryPrizeRequest)

    suspend fun getPrediction(matchRequest: MatchRequest) = iplApiService.get().getPrediction(matchRequest)
}
