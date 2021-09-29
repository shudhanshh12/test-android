package `in`.okcredit.voice_first.ui.bulk_add.edit_draft

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import androidx.annotation.StringRes

interface EditDraftTransactionContract {
    data class State(
        val draftId: String,
        val draft: DraftTransaction? = null,
        val selectedDraftMerchant: DraftMerchant? = null,
    ) : UiState

    enum class EndState { SAVED, DELETED, EDIT_DISCARDED, LOAD_FAILED }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class SetInitialDraft(
            val draft: DraftTransaction,
        ) : PartialState()

        data class SetDraftMerchant(
            val merchant: DraftMerchant,
            val draft: DraftTransaction,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object Delete : Intent()
        object AdvancedSearchRequested : Intent()

        data class SelectDraftMerchant(val merchant: DraftMerchant) : Intent()
        data class Save(val transactionType: String, val amount: Long, val note: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object OpenSearchActivity : ViewEvent()

        data class EndActivity(val state: EndState) : ViewEvent()
        data class InitialDraftLoaded(val draft: DraftTransaction) : ViewEvent()
        data class ShowError(@StringRes val error: Int) : ViewEvent()
    }
}
