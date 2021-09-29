package `in`.okcredit.voice_first.ui.bulk_add.edit_draft.draft_merchants_list

import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_CUSTOMER
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_SUPPLIER
import `in`.okcredit.voice_first.databinding.ItemDraftMerchantBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DraftMerchantItemView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var draftMerchant: DraftMerchant

    private var isChecked = false

    private val bindings = ItemDraftMerchantBinding.inflate(LayoutInflater.from(ctx), this, true)

    @ModelProp
    fun setChecked(isChecked: Boolean) {
        this.isChecked = isChecked
    }

    @ModelProp
    fun setMerchant(draftMerchant: DraftMerchant) {
        this.draftMerchant = draftMerchant

        when (draftMerchant.merchantType) {
            MERCHANT_TYPE_CUSTOMER -> bindings.accountTypeIcon.setImageResource(R.drawable.ic_customer)
            MERCHANT_TYPE_SUPPLIER -> bindings.accountTypeIcon.setImageResource(R.drawable.ic_supplier)
        }

        bindings.title.text = draftMerchant.merchantName
        bindings.selection.isVisible = isChecked
    }

    @CallbackProp
    fun setListener(listener: DraftMerchantSelectedListener?) {
        bindings.root.setOnClickListener { listener?.onSelected(draftMerchant) }
    }
}
