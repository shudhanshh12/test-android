package merchant.okcredit.gamification.ipl.leaderboard.epoxy.view

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.IplLeadboardPrizeHeaderBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PrizeHeader @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    deffStyle: Int = 0
) : FrameLayout(context, attributeSet, deffStyle) {

    private val binding = IplLeadboardPrizeHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setPrizeHeaderIcon(icon: Int) {
        binding.tvHeading.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
    }

    @ModelProp
    fun setPrizeHeaderTes(text: String) {
        binding.tvHeading.text = text
    }

    @ModelProp
    fun setPrizeHeaderColor(color: Int) {
        binding.tvHeading.background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}
