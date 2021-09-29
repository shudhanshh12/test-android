package `in`.okcredit.collection_ui.ui.referral.invite_list.views

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ReferralInviteListHeadingBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TargetedReferralListHeading @JvmOverloads constructor(
    private val ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: ReferralInviteListHeadingBinding =
        ReferralInviteListHeadingBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setComingFromLedger(comingFrmLedger: Boolean) {
        binding.apply {
            if (comingFrmLedger) {
                tvSubHeading.text = ctx.getString(R.string.other_merchants)
                vwTop.gone()
            } else {
                tvSubHeading.text = ctx.getString(R.string.select_from_the_list_and_invite)
                vwTop.visible()
            }
        }
    }
}
