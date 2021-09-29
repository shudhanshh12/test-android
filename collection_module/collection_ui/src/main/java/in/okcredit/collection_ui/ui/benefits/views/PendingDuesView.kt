package `in`.okcredit.collection_ui.ui.benefits.views

import `in`.okcredit.collection_ui.databinding.PendingDuesViewBinding
import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class PendingDuesView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val pendingDuesViewBinding = PendingDuesViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var currentAnimation: ViewPropertyAnimator? = null

    override fun onDetachedFromWindow() {
        currentAnimation?.cancel()
        currentAnimation = null
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        revealCustomerOne()
    }

    fun revealCustomerOne() {
        if ((pendingDuesViewBinding.imageCustomerOne.alpha == 0f)) {
            currentAnimation = pendingDuesViewBinding.imageCustomerOne.animate()
                .setStartDelay(100)
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        revealCustomerTwo()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        pendingDuesViewBinding.imageCustomerTwo.apply {
                            scaleX = 1f
                            scaleY = 1f
                            alpha = 1f
                        }
                        pendingDuesViewBinding.imageCustomerThree.apply {
                            scaleX = 1f
                            scaleY = 1f
                            alpha = 1f
                        }
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                })
        }
    }

    internal fun revealCustomerTwo() {
        currentAnimation = pendingDuesViewBinding.imageCustomerTwo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    revealCustomerThree()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    pendingDuesViewBinding.imageCustomerThree.apply {
                        scaleX = 1f
                        scaleY = 1f
                        alpha = 1f
                    }
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
    }

    internal fun revealCustomerThree() {
        currentAnimation = pendingDuesViewBinding.imageCustomerThree.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
    }
}
