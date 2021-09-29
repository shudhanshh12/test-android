package `in`.okcredit.collection_ui.ui.referral.invite_list.views

import `in`.okcredit.collection_ui.databinding.ReferralInviteHeaderViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TargetedReferralHeaderView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: ReferralInviteHeaderViewBinding =
        ReferralInviteHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
}
