package tech.okcredit.home.ui.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.home_fragment_inapp_downloading_loader.view.*
import tech.okcredit.home.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HomeScreenInAppUpdateLoaderView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(ctx).inflate(R.layout.home_fragment_inapp_downloading_loader, this, true)
        iv_refresh.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
    }
}
