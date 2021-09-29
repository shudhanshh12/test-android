package `in`.okcredit.merchant.customer_ui.ui.staff_link.education

import `in`.okcredit.merchant.customer_ui.databinding.ItemStaffLinkEducationBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.getDrawableCompact

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class StaffLinkEducationItem @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = ItemStaffLinkEducationBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setGraphicImage(image: Int) {
        binding.imageGraphic.setImageDrawable(context.getDrawableCompact(image))
    }

    @ModelProp
    fun setDescription(description: String) {
        binding.textDescription.text = description
    }

    @ModelProp
    fun setIndicatorPosition(position: Int) {
        binding.indicator.setSelected(position)
    }
}
