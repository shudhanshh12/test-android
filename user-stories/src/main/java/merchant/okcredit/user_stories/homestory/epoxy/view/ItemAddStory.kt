package merchant.okcredit.user_stories.homestory.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.user_stories.databinding.ItemAddStoryBinding

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ItemAddStory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = ItemAddStoryBinding.inflate(LayoutInflater.from(context), this, true)

    @CallbackProp
    fun setAddStoryClickedListener(onAddStoryClicked: (() -> Unit)?) {
        binding.rootView.setOnClickListener {
            onAddStoryClicked?.invoke()
        }
    }
}
