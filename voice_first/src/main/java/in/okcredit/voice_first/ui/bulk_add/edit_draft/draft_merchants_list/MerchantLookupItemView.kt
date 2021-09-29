package `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list

import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.databinding.ItemDraftMerchantBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MerchantLookupItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val bindings = ItemDraftMerchantBinding.inflate(LayoutInflater.from(ctx), this, true)

    @CallbackProp
    fun setListener(listener: DraftMerchantSelectedListener?) {
        bindings.root.setOnClickListener { listener?.onExtendedSearch() }
        bindings.title.setText(R.string.t_004_bulk_voice_txn_select_name_bottomsheet_search_more)
        bindings.accountTypeIcon.setImageResource(R.drawable.ic_search_black_24dp)
    }
}
