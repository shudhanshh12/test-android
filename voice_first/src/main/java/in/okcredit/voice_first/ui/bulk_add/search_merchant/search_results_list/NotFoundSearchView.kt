package `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list

import `in`.okcredit.voice_first.databinding.ItemSearchNotFoundBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.setHtmlText

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class NotFoundSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ItemSearchNotFoundBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setNoUserFoundMessage(message: String) {
        binding.tvMessage.setHtmlText(message)
    }
}
