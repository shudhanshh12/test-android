package merchant.okcredit.gamification.ipl.sundaygame.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplGameCompletedBoosterBinding
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel
import tech.okcredit.android.base.extensions.getString

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class CompletedBooster @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = IplGameCompletedBoosterBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setCardData(completedBoosterCard: SundayGameModel.CompletedBoosterCard) {
        when (completedBoosterCard.cardNumber) {
            1 -> {
                binding.tvBoosterDetail.text = getString(R.string.completed_first_booster)
                binding.tvBoosterDetail.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_booster_thumbs_up,
                    0
                )
            }
            2 -> {
                binding.tvBoosterDetail.text = getString(R.string.completed_second_booster)
                binding.tvBoosterDetail.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_booster_thumbs_up,
                    0
                )
            }
            3 -> {
                val drawable = VectorDrawableCompat.create(resources, R.drawable.ic_success_trophy, null)
                binding.tvBoosterDetail.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                binding.tvBoosterDetail.text =
                    resources.getString(R.string.completed_third_booster, (completedBoosterCard.totalRuns ?: 0))
            }
        }
    }
}
