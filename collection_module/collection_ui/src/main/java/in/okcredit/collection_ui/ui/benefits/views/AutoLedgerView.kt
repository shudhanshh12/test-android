package `in`.okcredit.collection_ui.ui.benefits.views

import `in`.okcredit.collection_ui.databinding.AutoLedgerViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.OnVisibilityStateChanged
import com.airbnb.epoxy.VisibilityState

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class AutoLedgerView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding = AutoLedgerViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentAnimation: ViewPropertyAnimator? = null

    override fun onDetachedFromWindow() {
        currentAnimation?.cancel()
        currentAnimation = null
        super.onDetachedFromWindow()
    }

    @OnVisibilityStateChanged
    fun revealOnlineTxn(@VisibilityState.Visibility visibilityState: Int) {
        if (binding.imageOnlineTxn.alpha == 0f && visibilityState == VisibilityState.FULL_IMPRESSION_VISIBLE) {
            currentAnimation = binding.imageOnlineTxn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
        }
    }
}
