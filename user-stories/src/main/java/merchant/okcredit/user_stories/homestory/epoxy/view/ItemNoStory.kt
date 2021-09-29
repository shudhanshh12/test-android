package merchant.okcredit.user_stories.homestory.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.user_stories.databinding.ItemAddStoryEmptyStatusBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemNoStory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding = ItemAddStoryEmptyStatusBinding.inflate(LayoutInflater.from(context), this, true)

    @CallbackProp
    fun setAddStoryClickedListener(onAddStoryClicked: (() -> Unit)?) {
        binding.rootView.setOnClickListener {
            onAddStoryClicked?.invoke()
        }
    }
}
