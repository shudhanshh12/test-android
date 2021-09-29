package `in`.okcredit.voice_first.ui.bulk_add.drafts_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftProcessingState
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftTransaction

sealed class DraftsListItem {

    data class ListSummary(
        val customerCount: Int,
        val supplierCount: Int,
    ) : DraftsListItem()

    data class DraftItem(
        val draftTransaction: DraftTransaction,
        val draftState: DraftProcessingState,
    ) : DraftsListItem()
}
