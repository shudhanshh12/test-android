package merchant.okcredit.user_stories.homestory

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.user_stories.contract.model.HomeStories

interface HomeUserStoryContract {

    data class State(
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val homeUserStory: HomeStories? = null,
        val isUserStoryEnabled: Boolean = false,
        val activeMerchantId: String = "",
        val activeMyStoryCount: Int = 0,
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {

        object ErrorState : PartialState()
        object NoChange : PartialState()
        data class SetHomeStories(
            val homeUserStory: HomeStories?,
        ) : PartialState()

        data class SetLoading(val loading: Boolean) : PartialState()
        data class SetUserStoryEnable(val enabled: Boolean) : PartialState()
        data class SetActiveMerchantId(val activeMerchantId: String) : PartialState()
        data class SetActiveMyStoryCount(val activeMyStoryCount: Int) : PartialState()
    }

    interface Interactor
}
