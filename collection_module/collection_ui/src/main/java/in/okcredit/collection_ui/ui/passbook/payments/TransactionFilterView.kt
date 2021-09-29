package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection_ui.databinding.ItemTransactionTypeFilterBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.getString

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TransactionFilterView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: ItemTransactionTypeFilterBinding =
        ItemTransactionTypeFilterBinding.inflate(LayoutInflater.from(context), this)

    private var mListener: Listener? = null

    private var transactionFilter: TransactionFilter? = null

    interface Listener {
        fun onClick(filter: TransactionFilter)
    }

    init {
        binding.root.setOnClickListener {
            transactionFilter?.let { it1 -> mListener?.onClick(it1) }
        }
    }

    data class FilterOption(
        val transactionFilter: TransactionFilter,
        @DrawableRes val icon: Int,
        @StringRes val text: Int,
        val isSelected: Boolean = false,
    )

    @ModelProp
    fun setData(filterOption: FilterOption) {
        this.transactionFilter = filterOption.transactionFilter
        binding.textFilter.text = getString(filterOption.text)
        binding.imageFilter.setImageDrawable(context.getDrawableCompact(filterOption.icon))
        binding.radioSelected.isChecked = filterOption.isSelected
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        mListener = listener
    }
}
