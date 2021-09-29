package `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list

import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import com.airbnb.epoxy.AsyncEpoxyController
import javax.inject.Inject

class DraftMerchantController @Inject constructor() : AsyncEpoxyController() {

    private lateinit var selectedId: String
    private lateinit var entities: List<DraftMerchant>
    private lateinit var selectedListener: DraftMerchantSelectedListener

    fun setData(
        selectedId: String,
        data: List<DraftMerchant>,
        listener: DraftMerchantSelectedListener,
    ) {
        this.selectedId = selectedId
        this.entities = data
        this.selectedListener = listener
        requestModelBuild()
    }

    override fun buildModels() {
        entities.forEach {
            draftMerchantItemView {
                id(it.merchantId)

                merchant(it)
                checked(it.merchantId == selectedId)
                listener(selectedListener)
            }
        }

        merchantLookupItemView {
            id("extended_merchant_lookup")

            listener(selectedListener)
        }
    }
}
