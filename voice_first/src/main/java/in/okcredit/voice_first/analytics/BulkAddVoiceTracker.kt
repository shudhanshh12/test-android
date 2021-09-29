package `in`.okcredit.voice_first.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.AMOUNT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.COMPLETED_COUNT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.DAY_OF_MONTH
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.DRAFT_TRANSACTION_ID
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.FAILED_COUNT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.INCOMPLETE_COUNT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.IS_AMOUNT_PRESENT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.IS_NAME_PRESENT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.IS_NOTE_PRESENT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.IS_SELECTION_CHANGED
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.IS_TYPE_PRESENT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.MONTH
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.NAME
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.PROCESSING_COUNT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.SIZE
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.TRANSACTIONS_COUNT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.TRANSCRIPT
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.TYPE
import `in`.okcredit.voice_first.analytics.BulkAddVoiceTracker.Companion.Property.YEAR
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftProcessingState
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction
import `in`.okcredit.voice_first.ui.bulk_add.edit_draft.EditDraftTransactionContract
import dagger.Lazy
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class BulkAddVoiceTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {
    companion object {

        object Event {
            const val DATE_SELECTOR_OPENED = "Voice Add: Date Selector Opened"
            const val DATE_SELECTOR_OK = "Voice Add: Date Selector OK"
            const val DATE_SELECTOR_CANCEL = "Voice Add: Date Selector Cancel"
            const val MIC_CLICKED = "Voice Add: Mic Clicked"
            const val TRANSACTION_TRANSCRIPT_SHOWN = "Voice Add: Transaction Transcript Shown"
            const val TRANSACTION_DRAFT_ADDED = "Voice Add: Transaction Draft Added"
            const val TRANSACTION_EDIT_OPENED = "Voice Add: Transaction Edit Opened"
            const val TRANSACTION_SUGGESTED_NAME_SELECTED = "Voice Add: Transaction Suggested name selected"
            const val SEARCH_NAME_OPENED = "Voice Add: Search Name Opened"
            const val SEARCH_NAME_SELECTED = "Voice Add: Search Name Selected"
            const val TRANSACTION_UPDATED = "Voice Add: Transaction Updated"
            const val TRANSACTION_DELETED = "Voice Add: Transaction Deleted"
            const val TRANSACTION_EDIT_CLOSED = "Voice Add: Transaction Edit Closed"
            const val SAVE_CLICKED = "Voice Add: Save Clicked"
            const val CANCEL_CLICKED = "Voice Add: Cancel Clicked"
            const val CANCEL_CONFIRMED = "Voice Add: Cancel Confirmed"
            const val SAVE_CONFIRMED = "Voice Add: Save Confirmed"
            const val TRANSACTION_ADDED = "Voice Add: Transaction Added"
        }

        object Property {
            const val DAY_OF_MONTH = "day_of_month"
            const val MONTH = "month"
            const val YEAR = "year"
            const val TRANSCRIPT = "transcript"
            const val DRAFT_TRANSACTION_ID = "transaction_id"
            const val NAME = "name"
            const val TYPE = "type"
            const val AMOUNT = "amount"
            const val IS_NAME_PRESENT = "is_name_present"
            const val IS_TYPE_PRESENT = "is_type_present"
            const val IS_AMOUNT_PRESENT = "is_amount_present"
            const val IS_NOTE_PRESENT = "is_note_present"
            const val IS_SELECTION_CHANGED = "is_selection_changed"
            const val SIZE = "size"
            const val TRANSACTIONS_COUNT = "transactions_count"
            const val COMPLETED_COUNT = "completed_count"
            const val INCOMPLETE_COUNT = "incomplete_count"
            const val PROCESSING_COUNT = "processing_count"
            const val FAILED_COUNT = "failed_count"
        }
    }

    fun logDateSelectorOpened(dayOfMonth: Int, month: Int, year: Int) {
        analyticsProvider.get().trackEvents(
            Event.DATE_SELECTOR_OPENED,
            mapOf(
                DAY_OF_MONTH to dayOfMonth,
                MONTH to month,
                YEAR to year,
            )
        )
    }

    fun logDateSelectorOk(dayOfMonth: Int, month: Int, year: Int) {
        analyticsProvider.get().trackEvents(
            Event.DATE_SELECTOR_OK,
            mapOf(
                DAY_OF_MONTH to dayOfMonth,
                MONTH to month,
                YEAR to year,
            )
        )
    }

    fun logDateSelectorCancel(dayOfMonth: Int, month: Int, year: Int) {
        analyticsProvider.get().trackEvents(
            Event.DATE_SELECTOR_CANCEL,
            mapOf(
                DAY_OF_MONTH to dayOfMonth,
                MONTH to month,
                YEAR to year,
            )
        )
    }

    fun logMicClicked() {
        analyticsProvider.get().trackEvents(Event.MIC_CLICKED)
    }

    fun logTransactionTranscriptShown(transcript: String) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_TRANSCRIPT_SHOWN,
            mapOf(TRANSCRIPT to transcript)
        )
    }

    fun logTransactionDraftAdded(parsed: DraftTransaction) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_DRAFT_ADDED,
            mapOf(
                DRAFT_TRANSACTION_ID to parsed.draftTransactionId,
                IS_NAME_PRESENT to !parsed.draftMerchants.isNullOrEmpty(),
                IS_TYPE_PRESENT to (parsed.transactionType != null),
                IS_AMOUNT_PRESENT to (parsed.amount != null && parsed.amount > 0)
            )
        )
    }

    fun logTransactionEditOpened(draftId: String) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_EDIT_OPENED,
            mapOf(DRAFT_TRANSACTION_ID to draftId)
        )
    }

    fun logTransactionSuggestedNameSelected(draftTransactionId: String?, isSelectionChanged: Boolean) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_SUGGESTED_NAME_SELECTED,
            mapOf(
                DRAFT_TRANSACTION_ID to draftTransactionId.toString(),
                IS_SELECTION_CHANGED to isSelectionChanged

            )
        )
    }

    fun logSearchNameOpened(draftTransactionId: String?, size: Int) {
        analyticsProvider.get().trackEvents(
            Event.SEARCH_NAME_OPENED,
            mapOf(
                DRAFT_TRANSACTION_ID to draftTransactionId.toString(),
                SIZE to size
            )
        )
    }

    fun logSearchNameSelected(draftTransactionId: String?) {
        analyticsProvider.get().trackEvents(
            Event.SEARCH_NAME_SELECTED,
            mapOf(DRAFT_TRANSACTION_ID to draftTransactionId.toString())
        )
    }

    fun logTransactionUpdated(
        draftTransactionId: String?,
        selectedMerchant: DraftMerchant?,
        uiDetails: EditDraftTransactionContract.Intent.Save,
    ) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_UPDATED,
            mapOf(
                DRAFT_TRANSACTION_ID to draftTransactionId.toString(),
                NAME to selectedMerchant?.merchantName.toString(),
                TYPE to uiDetails.transactionType,
                AMOUNT to uiDetails.amount,
                IS_NOTE_PRESENT to uiDetails.note.isNotNullOrBlank(),
            )
        )
    }

    fun logTransactionDeleted(draft: DraftTransaction?) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_DELETED,
            mapOf(DRAFT_TRANSACTION_ID to draft?.draftTransactionId.toString())
        )
    }

    fun logTransactionEditClosed(
        draftTransactionId: String?,
        selectedMerchant: DraftMerchant?,
        uiDetails: EditDraftTransactionContract.Intent.Save,
    ) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_EDIT_CLOSED,
            mapOf(
                DRAFT_TRANSACTION_ID to draftTransactionId.toString(),
                NAME to selectedMerchant?.merchantName.toString(),
                TYPE to uiDetails.transactionType,
                AMOUNT to uiDetails.amount,
                IS_NOTE_PRESENT to uiDetails.note.isNotNullOrBlank(),
            )
        )
    }

    fun logSaveClicked() {
        analyticsProvider.get().trackEvents(Event.SAVE_CLICKED)
    }

    fun logCancelClicked() {
        analyticsProvider.get().trackEvents(Event.CANCEL_CLICKED)
    }

    fun logCancelConfirmed(drafts: List<DraftTransaction>, statuses: Map<String, DraftProcessingState>) {
        val transactionsCount = drafts.size
        val completedCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.COMPLETE }
        val incompleteCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.INCOMPLETE }
        val processingCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.PROCESSING }
        val failedCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.FAILED }

        analyticsProvider.get().trackEvents(
            Event.CANCEL_CONFIRMED,
            mapOf(
                TRANSACTIONS_COUNT to transactionsCount,
                COMPLETED_COUNT to completedCount,
                INCOMPLETE_COUNT to incompleteCount,
                PROCESSING_COUNT to processingCount,
                FAILED_COUNT to failedCount,
            )
        )
    }

    fun logSaveConfirmed(drafts: List<DraftTransaction>, statuses: Map<String, DraftProcessingState>) {
        val transactionsCount = drafts.size
        val completedCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.COMPLETE }
        val incompleteCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.INCOMPLETE }
        val processingCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.PROCESSING }
        val failedCount = drafts.count { statuses[it.draftTransactionId] == DraftProcessingState.FAILED }

        analyticsProvider.get().trackEvents(
            Event.SAVE_CONFIRMED,
            mapOf(
                TRANSACTIONS_COUNT to transactionsCount,
                COMPLETED_COUNT to completedCount,
                INCOMPLETE_COUNT to incompleteCount,
                PROCESSING_COUNT to processingCount,
                FAILED_COUNT to failedCount,
            )
        )
    }

    fun logTransactionAdded(draftTransactionId: String) {
        analyticsProvider.get().trackEvents(
            Event.TRANSACTION_ADDED,
            mapOf(
                DRAFT_TRANSACTION_ID to draftTransactionId,
            )
        )
    }
}
