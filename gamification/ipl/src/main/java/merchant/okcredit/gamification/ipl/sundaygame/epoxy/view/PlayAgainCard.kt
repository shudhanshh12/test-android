package merchant.okcredit.gamification.ipl.sundaygame.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.IplPlayAgainCardBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PlayAgainCard @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = IplPlayAgainCardBinding.inflate(LayoutInflater.from(context), this, true)
}
