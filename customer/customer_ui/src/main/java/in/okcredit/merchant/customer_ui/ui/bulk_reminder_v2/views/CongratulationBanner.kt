package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views

import `in`.okcredit.merchant.customer_ui.databinding.BulkReminderCongraulationBannerBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class CongratulationBanner @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding = BulkReminderCongraulationBannerBinding
        .inflate(LayoutInflater.from(context), this, true)
}
