package `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list

import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_CUSTOMER
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_SUPPLIER
import `in`.okcredit.voice_first.databinding.ItemDraftMerchantBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MerchantSearchView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var draftMerchant: DraftMerchant

    private val bindings = ItemDraftMerchantBinding.inflate(LayoutInflater.from(ctx), this, true)

    @ModelProp
    fun setMerchant(draftMerchant: DraftMerchant) {
        this.draftMerchant = draftMerchant

        when (draftMerchant.merchantType) {
            MERCHANT_TYPE_CUSTOMER -> bindings.accountTypeIcon.setImageResource(R.drawable.ic_customer)
            MERCHANT_TYPE_SUPPLIER -> bindings.accountTypeIcon.setImageResource(R.drawable.ic_supplier)
        }

        bindings.title.text = draftMerchant.merchantName
    }

    @CallbackProp
    fun setListener(listener: SearchMerchantListener?) {
        bindings.root.setOnClickListener { listener?.onSelected(draftMerchant) }
    }
}
