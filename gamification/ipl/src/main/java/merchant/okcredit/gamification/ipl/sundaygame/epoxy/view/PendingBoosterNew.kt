package merchant.okcredit.gamification.ipl.sundaygame.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.PendingBoosterNewBinding
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PendingBoosterNew @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding: PendingBoosterNewBinding =
        PendingBoosterNewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setCardDetails(pendingBoosterCard: SundayGameModel.PendingBoosterCard) {
        binding.tvPoints.text = pendingBoosterCard.runs.toString()
        binding.tvEndsIn.text = if (pendingBoosterCard.endTime > 0) {
            resources?.getQuantityString(
                R.plurals.ends_in,
                pendingBoosterCard.endTime,
                pendingBoosterCard.endTime
            )
        } else {
            resources?.getString(R.string.ends_today)
        }

        if (pendingBoosterCard.isRunsCompleted) {
            binding.apply {
                ivRuns.setImageResource(R.drawable.ic_booster_completed_runs)
                tvRun.text = resources?.getString(R.string.horray)
                tvMakeWeeklyScore.text =
                    resources?.getString(
                        R.string.booster_completed_score,
                        pendingBoosterCard.runs.toString()
                    )
            }
        } else {
            binding.apply {
                tvRun.text = resources?.getQuantityString(
                    R.plurals.runs_away_weekly,
                    pendingBoosterCard.pendingRuns,
                    pendingBoosterCard.pendingRuns
                )
                tvMakeWeeklyScore.text =
                    resources?.getString(
                        R.string.make_weekly_score,
                        pendingBoosterCard.threadHoldRuns.toString()
                    )
            }
        }

        when (pendingBoosterCard.boosterState) {
            SundayGameModel.BoosterSate.NO_BOOSTER_DONE -> {
                binding.apply {
                    ivBooster.setImageResource(R.drawable.ic_do_booster)
                    tvBody.text =
                        resources?.getString(R.string.do_booster, pendingBoosterCard.threadHoldBooster.toString())
                    tvAvailableOn.text =
                        resources?.getString(R.string.complete_booster, pendingBoosterCard.threadHoldBooster.toString())
                }
            }
            SundayGameModel.BoosterSate.BOOSTER_IN_PROGRESS -> {
                binding.apply {
                    ivBooster.setImageResource(R.drawable.ic_booster_half)
                    tvBody.text = resources?.getString(R.string.keep_going)
                    tvAvailableOn.text =
                        resources?.getString(R.string.complete_one_motes_task)
                }
            }
            SundayGameModel.BoosterSate.BOOSTER_COMPLETED -> {
                binding.apply {
                    ivBooster.setImageResource(R.drawable.ic_booster_completed)
                    tvBody.text = resources?.getString(R.string.horray)
                    tvAvailableOn.text = resources?.getString(R.string.booster_completed)
                }
            }
        }
    }
}
