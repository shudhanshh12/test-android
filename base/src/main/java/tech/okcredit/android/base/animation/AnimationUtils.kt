package tech.okcredit.android.base.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.animation.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.isVisible

// Housekeeping : Return animation object as a reference in case the animation needs to be cancelled
object AnimationUtils {
    fun shake(view: View) {
        ObjectAnimator
            .ofFloat(view, "translationX", 0f, 15f, -15f, 15f, -15f, 10f, -10f, 4f, -4f, 0f)
            .setDuration(200)
            .start()
    }

    fun shakeV1(view: View) {
        ObjectAnimator
            .ofFloat(view, "translationX", 0f, 40f, -40f, 40f, -40f, 30f, -30f, 16f, -16f, 0f)
            .setDuration(500)
            .start()
    }

    fun shakeV2(view: View) {
        ObjectAnimator
            .ofFloat(view, "translationX", 0f, 40f, 0f, 40f, 0f, 30f, 0f, 16f, 0f)
            .setDuration(1000)
            .start()
    }

    @JvmStatic
    fun fadeIn(view: View) {
        ObjectAnimator
            .ofFloat(view, "alpha", 0f, 1f)
            .setDuration(300)
            .start()
    }

    @JvmStatic
    fun fadeInOnViewVisible(view: View) {
        if (view.isVisible) return
        ObjectAnimator
            .ofFloat(view, "alpha", 0f, 1f)
            .setDuration(300)
            .start()
    }

    @JvmStatic
    fun fadeOut(view: View) {
        ObjectAnimator
            .ofFloat(view, "alpha", 1f, 0f)
            .setDuration(300)
            .start()
    }

    fun blink(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "alpha", 1f, 0.1f).apply {
            duration = 1000
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    fun vibrate(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    fun expand(imageView: ImageView) {
        imageView.animate().setDuration(300).rotationBy(180f).start()
    }

    fun collapse(imageView: ImageView) {
        imageView.animate().setDuration(300).rotationBy(-180f).start()
    }

    fun translate(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 20f, 0f, -20f)
        animator.duration = 500
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    fun translateV1(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, -600f, 0f)
        animator.duration = 2000
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    fun scale(view: View) {
        val anim = ObjectAnimator.ofFloat(view, "scaleX", 0.5f)
        anim.duration = 500
        anim.repeatCount = ObjectAnimator.INFINITE
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

        // Make the object height 50%
        val anim2 = ObjectAnimator.ofFloat(view, "scaleY", 0.5f)
        anim2.duration = 500 // duration 3 seconds
        anim2.repeatCount = ObjectAnimator.INFINITE
        anim2.repeatMode = ObjectAnimator.REVERSE
        anim2.interpolator = LinearInterpolator()
        anim2.start()

        val anim3 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        anim3.duration = 500 // duration 3 seconds
        anim3.repeatCount = ObjectAnimator.INFINITE
        anim3.repeatMode = ObjectAnimator.REVERSE
        anim3.interpolator = AccelerateDecelerateInterpolator()
        anim3.start()
    }

    fun scaleV2(view: View) {
        val anim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f)
        anim.duration = 500
        anim.repeatCount = ObjectAnimator.INFINITE
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
    }

    fun shineEffect(view: View, shineView: View) {
        shineView.visibility = View.VISIBLE
        val animation: Animation = TranslateAnimation(0F, (view.width + (2 * shineView.width)).toFloat(), 0f, 0f)
        animation.duration = 1000
        animation.fillAfter = false
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.repeatMode = ObjectAnimator.REVERSE
        animation.interpolator = AccelerateDecelerateInterpolator()
        shineView.startAnimation(animation)
    }

    fun scaleV1(view: View) {
        val anim = ObjectAnimator.ofFloat(view, "scaleX", 0.5f)
        anim.duration = 500
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

        // Make the object height 50%
        val anim2 = ObjectAnimator.ofFloat(view, "scaleY", 0.5f)
        anim2.duration = 500 // duration 3 seconds
        anim2.repeatMode = ObjectAnimator.REVERSE
        anim2.interpolator = LinearInterpolator()
        anim2.start()
    }

    fun fadeInV1(view: View) {
        val anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        anim.setDuration(300)
            .addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(
                    animation: Animator,
                    isReverse: Boolean,
                ) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(
                    animation: Animator,
                    isReverse: Boolean,
                ) {
                    view.visibility = View.VISIBLE
                }
            })
        anim.start()
    }

    fun fadeOutV1(view: View) {
        val anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        anim.setDuration(300)
            .addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(
                    animation: Animator,
                    isReverse: Boolean,
                ) {
                    view.visibility = View.GONE
                }

                override fun onAnimationEnd(
                    animation: Animator,
                    isReverse: Boolean,
                ) {
                    view.visibility = View.GONE
                }
            })
        anim.start()
    }

    fun bounce(view: View, duration: Long = 1000, delta: Float = -100f, needStartDelay: Boolean = true) {
        val bounce = ObjectAnimator.ofFloat(view, "translationY", delta, 0f)
        bounce.duration = duration // 1sec
        bounce.interpolator = BounceInterpolator()
        bounce.repeatCount = 0
        if (needStartDelay) bounce.startDelay = 1000
        bounce.start()
    }

    fun upDownMotion(view: View, delta: Float = 100f, startDelay: Long = 1000L): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "translationY", delta, 0f).apply {
            duration = 400
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            this.startDelay = startDelay
            start()
        }
    }

    fun leftRightMotion(view: View) {
        val animation = ObjectAnimator.ofFloat(view, "translationX", -100f, 0f)
        animation.duration = 400
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.repeatMode = ObjectAnimator.REVERSE
        animation.startDelay = 1000
        animation.start()
    }

    // Animation used for the Share n Earn Nudge
    fun leftRightMotionReferralNudge(view: View) {
        val animation = ObjectAnimator.ofFloat(view, "translationX", 0f, 28f, 0f).apply {
            duration = 800
            interpolator = LinearInterpolator()
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            startDelay = 500
        }
        animation.start()
    }

    @JvmStatic
    fun pendulumMotion(view: View) {
        val animation = RotateAnimation(
            -10f, 15f, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        animation.duration = 400
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.repeatMode = ObjectAnimator.REVERSE
        animation.start()
        view.animation = animation
    }

    fun rotationAnimation(view: View) {
        val rotate = RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.duration = 500
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()
        view.animation = rotate
        rotate.start()
    }

    fun imageViewAnimatedChange(v: ImageView, new_image: Drawable) {
        val c = v.context
        val anim_out: Animation = AnimationUtils.loadAnimation(c, android.R.anim.fade_out)
        val anim_in: Animation = AnimationUtils.loadAnimation(c, android.R.anim.fade_in)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                v.setImageDrawable(new_image)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {}
                })
                v.startAnimation(anim_in)
            }
        })
        anim_out.duration = 1000
        v.startAnimation(anim_out)
    }

    fun cursorAnimation(v: View) {
        val animation = AlphaAnimation(0.5f, 0f)
        animation.duration = 600
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        v.startAnimation(animation)
    }

    fun bounce(targetView: View, interpolator: Interpolator = DecelerateInterpolator()) {
        ObjectAnimator.ofFloat(targetView, "translationY", 0f, 28f, 0f).apply {
            this.interpolator = interpolator
            startDelay = 0
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
    }
}
