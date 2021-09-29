package `in`.okcredit.merchant.rewards.ui.rewards_screen

import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface RewardsContract {

    data class State(
        val networkError: Boolean = false,
        val loader: Boolean = true,
        val error: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: Int? = null,
        val rewards: List<RewardsControllerModel> = arrayListOf(),
        val sumOfClaimedRewards: Long = 0,
        val unclaimedRewards: Long = 0,
        val isCollectionAdopted: Boolean = false,
        val contextualHelpIds: List<String> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object ErrorState : PartialState()

        data class SetNetworkError(val status: Boolean) : PartialState()

        data class SetRewards(val rewards: List<RewardsControllerModel>, val amount: Long, val unclaimedRewards: Long) :
            PartialState()

        data class ShowAlert(val message: Int) : PartialState()

        object HideAlert : PartialState()

        data class SetCollectionAdopted(val enabled: Boolean) : PartialState()

        data class SetContextualHelpIds(val helpIds: List<String>) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object OnRefresh : Intent()

        object CheckCollectionStatus : Intent()

        object RemoveClaimError : Intent()

        object GetAllRewards : Intent()

        data class ShowAddMerchantDestinationDialog(
            val isUpdateCollection: Boolean,
            val paymentMethodType: String? = null,
            val source: String? = null
        ) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class ShowAddMerchantDestinationDialog(
            val unclaimedRewards: Long,
            val isUpdateCollection: Boolean,
            val paymentMethodType: String?,
            val source: String?
        ) : ViewEvent()
    }
}

fun List<RewardModel>.convertToRewardsControllerModels(): List<RewardsControllerModel> =
    this.map { it.toControllerModel() }
