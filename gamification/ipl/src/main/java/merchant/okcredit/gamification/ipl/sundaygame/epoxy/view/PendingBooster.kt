package merchant.okcredit.gamification.ipl.sundaygame.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.databinding.IplGamePendingBoosterBinding
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PendingBooster @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding: IplGamePendingBoosterBinding =
        IplGamePendingBoosterBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setCardDetails(pendingBoosterCard: SundayGameModel.PendingBoosterCard) {
//        binding.tvCardNumber.text = pendingBoosterCard.cardNumber.toString()
//        binding.tvTime.text = if (pendingBoosterCard.endTime > 0) {
//            resources?.getQuantityString(
//                R.plurals.ends_in,
//                pendingBoosterCard.endTime,
//                pendingBoosterCard.endTime
//            )
//        } else {
//            getString(R.string.ends_today)
//        }
//
//        binding.ivBoosterIcon.setImageResource(R.drawable.ic_pending_booster)
//        when (pendingBoosterCard.cardNumber) {
//            1 -> {
//                binding.tvBoosterTitle.text = getString(R.string.title_first_booster_task)
//                binding.tvBoosterBody.text = getString(R.string.complete_first_booster)
//            }
//            2 -> {
//                binding.tvBoosterTitle.text = getString(R.string.title_second_booster_task)
//                binding.tvBoosterBody.text = getString(R.string.complete_second_booster)
//            }
//            3 -> {
//                binding.tvBoosterTitle.text = resources?.getQuantityString(
//                    R.plurals.runs_away,
//                    pendingBoosterCard.pendingRuns ?: 0,
//                    pendingBoosterCard.pendingRuns ?: 0
//                )
//
//                binding.tvBoosterBody.text = getString(R.string.complete_runs_tip)
//            }
//        }
    }
}
