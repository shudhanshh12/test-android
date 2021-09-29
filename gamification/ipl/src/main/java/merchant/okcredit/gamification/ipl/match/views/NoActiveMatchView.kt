package merchant.okcredit.gamification.ipl.match.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.NoActiveMatchViewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class NoActiveMatchView @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = NoActiveMatchViewBinding.inflate(LayoutInflater.from(context), this)
}
