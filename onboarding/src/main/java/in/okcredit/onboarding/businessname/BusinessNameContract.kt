package `in`.okcredit.onboarding.businessname

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BusinessNameContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val isMerchantFromCollectionCampaign: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        object ErrorState : PartialState()

        object NoChange : PartialState()

        object SetNetworkError : PartialState()

        data class IsMerchantFromCollectionCampaign(val isMerchantFromCollectionCampaign: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        data class BusinessName(val businessName: String) : Intent()

        data class NameSkipped(val key: String, val value: Boolean) : Intent()
    }

    interface Navigator {
        fun gotoLogin()

        fun goToHome()
    }
}
