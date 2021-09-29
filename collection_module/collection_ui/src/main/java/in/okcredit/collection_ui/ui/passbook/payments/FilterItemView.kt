package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.collection_ui.databinding.FilterViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.rd.utils.DensityUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class FilterItemView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: FilterViewBinding = FilterViewBinding.inflate(LayoutInflater.from(context), this)
    private var mListener: Listener? = null

    interface Listener {
        fun onClick(filter: String)
    }

    init {
        binding.root.setPadding(DensityUtils.dpToPx(16))
    }

    data class FilterOption(val text: String, val isSelected: Boolean)

    @ModelProp
    fun setData(filterOption: FilterOption) {
        binding.label.text = filterOption.text
        binding.selected.isVisible = (filterOption.isSelected)
        binding.root.setOnClickListener {
            mListener?.onClick(filterOption.text)
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        mListener = listener
    }
}
