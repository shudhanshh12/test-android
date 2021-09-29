package `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant

interface SearchMerchantListener {

    fun onSelected(merchant: DraftMerchant)
}
