package `in`.okcredit.voice_first.ui.bulk_add.search_merchant

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list.SearchItem

interface SearchMerchantContract {

    data class State(
        val isLoading: Boolean = true,
        val customers: List<DraftMerchant> = listOf(),
        val suppliers: List<DraftMerchant> = listOf(),
        val searchQuery: String = "",
        val hideSearchInput: Boolean = false,
        val itemList: List<SearchItem> = listOf(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object HideLoading : PartialState()

        object NoChange : PartialState()

        data class SetData(
            val customers: List<DraftMerchant>,
            val suppliers: List<DraftMerchant>,
        ) : PartialState()

        data class UpdateSearchQuery(val searchQuery: String) : PartialState()

        data class SetSearchInput(val hide: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class SearchQuery(val searchQuery: String) : Intent()

        object ResetData : Intent()

        data class ShowSearchInput(val canShow: Boolean = true) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object ShowError : ViewEvent()

        object ShowKeyboard : ViewEvent()

        object ShowInternetError : ViewEvent()
    }
}
