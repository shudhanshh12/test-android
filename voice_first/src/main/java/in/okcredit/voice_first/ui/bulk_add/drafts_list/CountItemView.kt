package `in`.okcredit.voice_first.ui.bulk_add.drafts_list

import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.databinding.ItemDraftTransactionCountsBinding
import `in`.okcredit.voice_first.ui.bulk_add.drafts_list.DraftsListItem.ListSummary
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class CountItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var listSummary: ListSummary

    private val bindings = ItemDraftTransactionCountsBinding.inflate(LayoutInflater.from(ctx), this, true)

    @ModelProp
    fun setListSummary(listSummary: ListSummary) {
        this.listSummary = listSummary
        render()
    }

    private fun render() {
        bindings.customerCount.text = context.getString(
            R.string.t_004_bulk_voice_txn_customer_count,
            listSummary.customerCount.toString()
        )
        bindings.supplierCount.text = context.getString(
            R.string.t_004_bulk_voice_txn_supplier_count,
            listSummary.supplierCount.toString()
        )
    }
}
