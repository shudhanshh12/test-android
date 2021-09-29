package tech.okcredit.home.ui.customer_tab.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import tech.okcredit.home.databinding.HomeAddCustomerTutorialNudgeV3Binding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AddCustomerTutorialNudgeV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: HomeAddCustomerTutorialNudgeV3Binding =
        HomeAddCustomerTutorialNudgeV3Binding.inflate(LayoutInflater.from(context), this, true)
}
