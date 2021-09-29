package merchant.okcredit.gamification.ipl.match.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.ItemMatchLoadingBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MatchLoadingView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ItemMatchLoadingBinding.inflate(LayoutInflater.from(context), this, true)
}
