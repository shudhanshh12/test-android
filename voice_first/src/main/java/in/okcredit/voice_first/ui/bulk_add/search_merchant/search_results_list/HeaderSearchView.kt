package `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list

import `in`.okcredit.voice_first.databinding.ItemSearchHeaderBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HeaderSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ItemSearchHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }
}
