package `in`.okcredit.frontend.ui.live_sales.views

import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PictureOnboardingView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(ctx).inflate(R.layout.picture_onboarding_view, this, true)
    }
}
