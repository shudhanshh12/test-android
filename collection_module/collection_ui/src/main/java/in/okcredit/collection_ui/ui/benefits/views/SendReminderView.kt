package `in`.okcredit.collection_ui.ui.benefits.views

import `in`.okcredit.collection_ui.databinding.SendReminderViewBinding
import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.OnVisibilityStateChanged
import com.airbnb.epoxy.VisibilityState
import com.airbnb.epoxy.VisibilityState.FULL_IMPRESSION_VISIBLE

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class SendReminderView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val viewBinding = SendReminderViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentAnimation: ViewPropertyAnimator? = null

    override fun onDetachedFromWindow() {
        currentAnimation?.cancel()
        currentAnimation = null
        super.onDetachedFromWindow()
    }

    @OnVisibilityStateChanged
    fun revealRemindButton(@VisibilityState.Visibility visibilityState: Int) {
        if (viewBinding.imageRemindButton.alpha == 0f && visibilityState == FULL_IMPRESSION_VISIBLE) {
            currentAnimation = viewBinding.imageRemindButton.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        revealReminderMessage()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        viewBinding.imageReminderMessage.scaleX = 1f
                        viewBinding.imageReminderMessage.scaleY = 1f
                        viewBinding.imageReminderMessage.alpha = 1f
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                })
        }
    }

    internal fun revealReminderMessage() {
        currentAnimation = viewBinding.imageReminderMessage.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
    }
}
