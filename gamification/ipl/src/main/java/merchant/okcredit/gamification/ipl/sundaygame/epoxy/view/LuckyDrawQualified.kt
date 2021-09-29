package merchant.okcredit.gamification.ipl.sundaygame.epoxy.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.IplSundayGameQualifiedBinding
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LuckyDrawQualified @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = IplSundayGameQualifiedBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setLuckyDrawDate(time: Long) {
        val date = DateTimeUtils.getFormat1(DateTime(time))
        binding.tvTime.text = resources.getString(R.string.lucky_draw_available_on, date)
    }
}
