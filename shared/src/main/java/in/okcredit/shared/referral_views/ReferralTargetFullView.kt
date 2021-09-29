package `in`.okcredit.shared.referral_views

import `in`.okcredit.shared.databinding.ReferralTargetFullViewBinding
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ReferralTargetFullView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = ReferralTargetFullViewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setTarget(content: ReferralTargetBanner?) {
        if (!content?.title.isNullOrBlank()) {
            binding.title.text = content?.title
        }

        if (!content?.description.isNullOrBlank()) {
            binding.subtitle.text = content?.description
        }
    }
}
