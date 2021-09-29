package `in`.okcredit.collection_ui.ui.home.adoption

import `in`.okcredit.collection_ui.databinding.ViewCollectionValueBinding
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.getString

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class ValuePropositionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : LinearLayout(ctx, attrs, defStyleAttr) {

    private val binding = ViewCollectionValueBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
    }

    @ModelProp
    fun setAdoptionItem(collectionAdoptionItem: CollectionAdoptionItem) {
        binding.textTitle.text = getString(collectionAdoptionItem.title)
        binding.imageIllustration.setImageDrawable(context.getDrawableCompact(collectionAdoptionItem.illustration))
    }
}
