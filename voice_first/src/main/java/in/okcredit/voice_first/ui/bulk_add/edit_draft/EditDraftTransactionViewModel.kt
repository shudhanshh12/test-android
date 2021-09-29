package `in`.okcredit.voice_first.ui.bulk_add.edit_draft

import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionContract.*
import `in`.okcredit.voice_first.usecase.bulk_add.DeleteDraftTransaction
import `in`.okcredit.voice_first.usecase.bulk_add.GetDraftTransaction
import `in`.okcredit.voice_first.usecase.bulk_add.PutDraftTransaction
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class EditDraftTransactionViewModel @Inject constructor(
    initialState: Lazy<State>,
    private val analytics: Lazy<BulkAddVoiceTracker>,
    private val getDraftTransaction: Lazy<GetDraftTransaction>,
    private val putDraftTransaction: Lazy<PutDraftTransaction>,
    private val deleteDraftTransaction: Lazy<DeleteDraftTransaction>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState.get()) {

    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(
            loadDraftTransaction(),

            handleDelete(),
            handleSave(),
            handleSetDraftMerchant(),
            handleAdvancedSearchRequested(),
        )
    }

    private fun loadDraftTransaction() = intent<Intent.Load>()
        .map { getCurrentState() }
        .switchMap { state ->
            wrap(
                rxSingle {
                    analytics.get().logTransactionEditOpened(state.draftId)
                    state.draftId
                        .takeIf { it.isNotNullOrBlank() }
                        ?.let { getDraftTransaction.get().execute(it) }
                        ?: error("Error loading draft from draftId ${state.draftId}")
                }
            )
        }
        .map {
            when (it) {
                is Result.Success -> {
                    emitViewEvent(ViewEvent.InitialDraftLoaded(it.value))
                    PartialState.SetInitialDraft(it.value)
                }
                is Result.Failure -> {
                    emitViewEvent(ViewEvent.EndActivity(EndState.LOAD_FAILED))
                    PartialState.NoChange
                }
                is Result.Progress -> PartialState.NoChange
            }
        }

    private fun handleDelete() = intent<Intent.Delete>()
        .map { getCurrentState() }
        .switchMap {
            wrap(
                rxCompletable {
                    deleteDraftTransaction.get().execute(it.draftId)
                    emitViewEvent(ViewEvent.EndActivity(EndState.DELETED))
                }
            )
        }
        .map { PartialState.NoChange }

    private fun handleSave() = intent<Intent.Save>()
        .map { it.toDraft(getCurrentState()) ?: error("Unable to save draft") }
        .switchMap {
            wrap(
                rxCompletable {
                    putDraftTransaction.get().execute(it)
                    emitViewEvent(ViewEvent.EndActivity(EndState.SAVED))
                }
            )
        }
        .map { PartialState.NoChange }

    private fun handleSetDraftMerchant() = intent<Intent.SelectDraftMerchant>()
        .map { it.merchant }
        .map { selectedMerchant ->
            val state = getCurrentState()
            val draft = state.draft ?: return@map PartialState.NoChange
            val existingIds = draft.draftMerchants?.map { it.merchantId }?.toSet()

            val updatedMerchants = when {
                existingIds == null -> listOf(selectedMerchant)
                existingIds.contains(selectedMerchant.merchantId) -> draft.draftMerchants
                else -> draft.draftMerchants.toMutableList().apply { add(selectedMerchant) }
            }

            PartialState.SetDraftMerchant(
                selectedMerchant,
                draft.copy(draftMerchants = updatedMerchants)
            )
        }

    private fun handleAdvancedSearchRequested() = intent<Intent.AdvancedSearchRequested>()
        .map {
            emitViewEvent(ViewEvent.OpenSearchActivity)
            PartialState.NoChange
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            is PartialState.NoChange -> currentState

            is PartialState.SetInitialDraft -> currentState.copy(
                draft = partialState.draft,
                selectedDraftMerchant = partialState.draft.draftMerchants?.firstOrNull()
            )

            is PartialState.SetDraftMerchant -> currentState.copy(
                draft = partialState.draft,
                selectedDraftMerchant = partialState.merchant,
            )
        }
    }

    private fun Intent.Save.toDraft(state: State): DraftTransaction? {
        val merchants = state.draft?.draftMerchants?.let { list ->
            val selectedMerchantId = state.selectedDraftMerchant?.merchantId
                ?: return@let list

            val posOfSelected = list.indexOfFirst { it.merchantId == selectedMerchantId }
                .takeIf { it >= 0 }
                ?: return@let list

            list.toMutableList().apply { add(0, removeAt(posOfSelected)) }
        }

        return state.draft?.copy(
            transactionType = transactionType,
            amount = amount,
            note = note,
            draftMerchants = merchants
        )
    }
}
