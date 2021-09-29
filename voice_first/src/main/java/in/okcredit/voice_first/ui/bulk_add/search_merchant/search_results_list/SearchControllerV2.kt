package `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list

import `in`.okcredit.voice_first.R
import android.content.Context
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController

class SearchControllerV2(
    private val context: Context,
    private val listener: SearchMerchantListener,
) : TypedEpoxyController<List<SearchItem>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
) {

    override fun buildModels(data: List<SearchItem>) {
        data.forEach {
            when (it) {
                is SearchItem.MerchantItem -> renderMerchantView(it)
                is SearchItem.HeaderItem -> renderHeaderView(it)
                is SearchItem.NoUserFoundItem -> renderNoUserFoundItem(it)
            }
        }
    }

    private fun renderMerchantView(merchantItem: SearchItem.MerchantItem) {
        merchantSearchView {
            id(merchantItem.draftMerchant.merchantId)

            merchant(merchantItem.draftMerchant)
            listener(listener)
        }
    }

    private fun renderHeaderView(headerItem: SearchItem.HeaderItem) {
        headerSearchView {
            id(headerItem.title)
            title(context.getString(headerItem.title))
        }
    }

    private fun renderNoUserFoundItem(noUserFoundItem: SearchItem.NoUserFoundItem) {
        notFoundSearchView {
            id("no_customer_view_1")
            noUserFoundMessage(context.getString(R.string.contact_not_found, noUserFoundItem.searchQuery))
        }
    }
}
