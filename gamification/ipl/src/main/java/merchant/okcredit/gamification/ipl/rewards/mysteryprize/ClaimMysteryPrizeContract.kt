package merchant.okcredit.gamification.ipl.rewards.mysteryprize

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ClaimMysteryPrizeContract {

    data class State(
        val claimed: Boolean = false,
        val amount: Int,
        val claimInProgress: Boolean = false,
        val welcomeReward: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object ClaimSuccessState : PartialState()

        object ClaimProgressState : PartialState()

        data class WelcomeReward(val welcomeReward: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object ClaimPrize : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object RewardWon : ViewEvent()

        object InternetIssue : ViewEvent()

        object ServerError : ViewEvent()
    }
}
