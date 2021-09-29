package `in`.okcredit.shared.referral_views

import `in`.okcredit.shared.R
import `in`.okcredit.shared.databinding.ReferralTargetBannerBinding
import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import `in`.okcredit.shared.referral_views.model.ReferralTargetBanner
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.Glide

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ReferralTargetBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = ReferralTargetBannerBinding.inflate(LayoutInflater.from(context), this, true)
    private var deeplinkNavigator: InternalDeeplinkNavigationDelegator? = null
    private var onTransactionInitiated: (() -> Unit)? = null
    private var onCloseClicked: (() -> Unit)? = null

    init {
        binding.close.setOnClickListener {
            onCloseClicked?.invoke()
        }
    }

    @CallbackProp
    fun setDeeplinkNavigator(deeplinkNavigator: InternalDeeplinkNavigationDelegator?) {
        this.deeplinkNavigator = deeplinkNavigator
    }

    @CallbackProp
    fun setTransactionListener(onTransactionInitiated: (() -> Unit)?) {
        this.onTransactionInitiated = onTransactionInitiated
    }

    @CallbackProp
    fun setCloseListener(onCloseClicked: (() -> Unit)?) {
        this.onCloseClicked = onCloseClicked
    }

    @ModelProp
    fun setTarget(content: ReferralTargetBanner?) {
        binding.apply {
            title.text = content?.title
            subtitle.text = content?.description
            root.setOnClickListener {
                deeplinkNavigator?.executeDeeplink(content?.deepLink ?: "")
                onTransactionInitiated?.invoke()
            }
            if (content?.icon != null) {
                Glide
                    .with(context)
                    .load(content.icon)
                    .placeholder(R.drawable.ic_roa_target_banner__73657)
                    .error(R.drawable.ic_roa_target_banner__73657)
                    .fallback(R.drawable.ic_roa_target_banner__73657)
                    .into(icon)
            }
        }
    }
}
