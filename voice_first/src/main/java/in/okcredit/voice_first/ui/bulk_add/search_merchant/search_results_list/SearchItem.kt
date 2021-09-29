package `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import androidx.annotation.StringRes

sealed class SearchItem {

    data class MerchantItem(
        val draftMerchant: DraftMerchant,
    ) : SearchItem()

    data class NoUserFoundItem(
        val searchQuery: String,
    ) : SearchItem()

    data class HeaderItem(
        @StringRes val title: Int,
    ) : SearchItem()
}
