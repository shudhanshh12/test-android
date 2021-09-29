package merchant.okcredit.gamification.ipl.game.ui

import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.gamification.ipl.game.data.server.model.response.BoosterQuestion
import merchant.okcredit.gamification.ipl.game.data.server.model.response.OnboardingDetails
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Player
import merchant.okcredit.gamification.ipl.game.data.server.model.response.PredictionResponse
import merchant.okcredit.gamification.ipl.game.data.server.model.response.Team
import merchant.okcredit.gamification.ipl.game.data.server.model.response.YoutubeLinks
import merchant.okcredit.gamification.ipl.game.utils.MatchStatusMapping
import merchant.okcredit.gamification.ipl.model.Booster
import merchant.okcredit.gamification.ipl.model.MatchStatus
import merchant.okcredit.gamification.ipl.model.PlayerScore
import merchant.okcredit.gamification.ipl.model.TeamScore
import merchant.okcredit.gamification.ipl.rewards.IplRewardsControllerModel
import merchant.okcredit.gamification.ipl.utils.IplUtils

interface GameContract {

    data class State(
        val isLoading: Boolean = true,
        val progressCardState: ProgressCardState = ProgressCardState(),
        val totalPoints: Int = 0,
        val isTeamSelectLoading: Boolean = false,
        val isBatsmanSelectLoading: Boolean = false,
        val isBowlersSelectLoading: Boolean = false,
        val progress: Float = 0f,
        val onboardingDetails: OnboardingDetails? = null,
        val homeTeamScore: TeamScore? = null,
        val awayTeamScore: TeamScore? = null,
        val batsmanScore: PlayerScore? = null,
        val bowlerScore: PlayerScore? = null,
        val networkError: Boolean = false,
        val serverError: Boolean = false,
        val gameExpired: Boolean = false,
        val matchStatusText: String = "",
        val chosenTeamWon: Boolean? = null,
        val winningTeamName: String = "",
        val boosterQuestion: BoosterQuestion.Question? = null,
        val showAllBatsman: Boolean = false,
        val showAllBowler: Boolean = false,
        val rewards: List<IplRewardsControllerModel> = listOf(),
        val boosterCompletedMultiplier: Int? = null, // booster multiplier will be initiated once booster is completed
        val prediction: PredictionResponse? = null,
        val winPoints: Int = 0,
        val youtubeUrl: String = ""

    ) : UiState {
        data class ProgressCardState(val progress: Int = 0, val completedSteps: Int = 0)

        fun isBoosterCompleted() = boosterCompletedMultiplier != null && boosterCompletedMultiplier > 0

        fun isMatchOver() = matchStatusText == MatchStatusMapping.STUMPS.status ||
            matchStatusText == MatchStatusMapping.ABANDONED.status

        private fun isBoosterPendingForActivation(): Boolean {
            if (onboardingDetails?.boosterStartTime != null) {
                return (IplUtils.getCurrentDateTime().time < (onboardingDetails.boosterStartTime * 1000))
            }
            return true
        }

        fun shouldShowPendingBooster(): Boolean {
            return onboardingDetails?.isQualified() == true && isBoosterPendingForActivation() && !isMatchOver() && boosterCompletedMultiplier == null
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object ShowLoading : PartialState()

        data class ShowTeamSelectLoading(val loading: Boolean) : PartialState()

        data class ShowBatsmanSelectLoading(val loading: Boolean) : PartialState()

        data class ShowBowlerSelectLoading(val loading: Boolean) : PartialState()

        object ShowNetworkError : PartialState()

        object ShowServerError : PartialState()

        data class OnboardingState(val onboardingDetails: OnboardingDetails) : PartialState()

        data class HomeTeamScoreState(val teamScore: TeamScore?) : PartialState()

        data class AwayTeamScoreState(val teamScore: TeamScore?) : PartialState()

        data class BatsmanScoreState(val batsmanScore: PlayerScore?) : PartialState()

        data class BowlerScoreState(val bowlerScore: PlayerScore?) : PartialState()

        object SetGameExpired : PartialState()

        object ShowAllBatsman : PartialState()

        object ShowAllBowlers : PartialState()

        data class MatchStatusState(val matchStatus: MatchStatus) : PartialState()

        data class BoosterTriggerState(val booster: Booster) : PartialState()

        data class BoosterQuestionState(val boosterQuestion: BoosterQuestion) : PartialState()

        data class BoosterCompleted(val choice: String) : PartialState()

        object BoosterClicked : PartialState()

        data class ShareIntentSuccess(val intent: android.content.Intent) : PartialState()

        object BoosterSuccessState : PartialState()

        object BoosterExpiredState : PartialState()

        data class Prediction(val prediction: PredictionResponse) : PartialState()

        data class SetYoutubeUrl(val url: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object Retry : Intent()

        object GameExpired : Intent()

        object BoosterClicked : Intent()

        data class SelectTeam(val choiceId: String) : Intent()

        data class SelectBatsman(val choiceId: String) : Intent()

        data class SelectBowlers(val choiceId: String) : Intent()

        data class GetTeamScore(val homeTeam: Team, val awayTeam: Team) : Intent()

        data class GetBatsmanScore(val batsman: Player) : Intent()

        data class GetBowlerScore(val bowler: Player) : Intent()

        data class BusinessTypeSelected(val businessType: BusinessType) : Intent()

        object BatmanLoadMore : Intent()

        object BowlerLoadMore : Intent()

        object BoosterTaskCompleted : Intent()

        object FetchBusinessTypes : Intent()

        object GetBoosterQuestion : Intent()

        object BoosterTrigger : Intent()

        object BoosterExpired : Intent()

        object FetchShareAppIntent : UserIntent

        object AddOkNumberInDevice : Intent()

        data class BoosterSubmitted(val choice: String, val question: String) : Intent()

        data class GetYoutubeLink(val youtubeLinks: List<YoutubeLinks>) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class ShowError(val error: Int) : ViewEvent()
        data class ShowSnackBar(val error: Int) : ViewEvent()
        data class ShowMCQ(val boosterQuestion: BoosterQuestion.Question) : ViewEvent()
        data class ShowOpenQuestion(val boosterQuestion: BoosterQuestion.Question) : ViewEvent()
        data class UpdateBusinessType(val list: List<BusinessType>) : ViewEvent()
        object UpdateBusinessCategory : ViewEvent()
        object UpdateMerchantName : ViewEvent()
        object UpdateMerchantEmail : ViewEvent()
        object UpdateMerchantAddress : ViewEvent()
        object UpdateMerchantAbout : ViewEvent()
        object SetupBankAccount : ViewEvent()
        object UpdateBusinessName : ViewEvent()
        object AddCustomer : ViewEvent()
        object AddTransaction : ViewEvent()
        data class RateUs(val appLink: String) : ViewEvent()
        object ShareBusinessCard : ViewEvent()
        object AddOkcNumberInPhone : ViewEvent()
        data class InstallOkShopApp(val appLink: String) : ViewEvent()
        data class ShareApp(val intent: android.content.Intent) : ViewEvent()
        data class VoiceCollection(val text: String) : ViewEvent()
    }
}
