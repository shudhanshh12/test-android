package merchant.okcredit.gamification.ipl.sundaygame.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.LuckyDrawQualifiedNewBinding
import merchant.okcredit.gamification.ipl.sundaygame.epoxy.controller.SundayGameModel
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LuckyDrawQualifiedNew @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = LuckyDrawQualifiedNewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setLuckyDrawDate(luckyDraw: SundayGameModel.LuckyDrawQualifiedCard) {
        val date = DateTimeUtils.getFormat4(DateTime(luckyDraw.date))
        binding.apply {
            tvEndsIn.text = if (luckyDraw.endTime > 0) {
                resources?.getQuantityString(
                    R.plurals.ends_in,
                    luckyDraw.endTime,
                    luckyDraw.endTime
                )
            } else {
                resources?.getString(R.string.ends_today)
            }
            tvAvailableOn.text = resources.getString(R.string.sunday_draw_availability, date)
            tvRun.text = luckyDraw.runs.toString()
        }
    }
}
