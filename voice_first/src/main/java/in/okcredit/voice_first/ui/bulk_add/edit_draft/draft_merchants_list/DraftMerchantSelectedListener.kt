package `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant

interface DraftMerchantSelectedListener {
    fun onSelected(draftMerchant: DraftMerchant)

    fun onExtendedSearch()

    fun onDismissed()
}
