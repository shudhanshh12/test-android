package `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views

import `in`.okcredit.user_migration.databinding.ItemFileParsingErrorBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemFileParsingError @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ItemFileParsingErrorBinding =
        ItemFileParsingErrorBinding.inflate(LayoutInflater.from(context), this, true)
}
