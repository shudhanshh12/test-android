package merchant.okcredit.gamification.ipl.game.ui.epoxy

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.ItemTeamNameBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ItemTeamName @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    private val binding: ItemTeamNameBinding = ItemTeamNameBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setTeamName(teamName: String?) {
        binding.tvTeamName.text = teamName
    }

    @ModelProp
    fun setTeamColor(teamColor: Int) {
        binding.tvTeamColor.background.setColorFilter(teamColor, PorterDuff.Mode.SRC_ATOP)
    }
}
