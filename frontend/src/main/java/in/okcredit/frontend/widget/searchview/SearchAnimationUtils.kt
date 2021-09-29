package `in`.okcredit.frontend.widget.searchview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.RequiresApi
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import java.util.*

object SearchAnimationUtils {
    const val ANIMATION_DURATION_DEFAULT = 250

    @JvmStatic
    @JvmOverloads
    fun revealOrFadeIn(
        view: View,
        duration: Int = ANIMATION_DURATION_DEFAULT,
        listener: AnimationListener? = null,
        center: Point? = null
    ): Animator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reveal(
                view,
                duration,
                listener,
                center
            )
        } else {
            fadeIn(
                view,
                duration,
                listener
            )
        }
    }

    @JvmStatic
    @JvmOverloads
    fun hideOrFadeOut(
        view: View,
        duration: Int = ANIMATION_DURATION_DEFAULT,
        listener: AnimationListener? = null,
        center: Point? = null
    ): Animator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hide(
                view,
                duration,
                listener,
                center
            )
        } else {
            fadeOut(
                view,
                duration,
                listener
            )
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun reveal(view: View, duration: Int): Animator {
        return reveal(
            view,
            duration,
            null,
            null
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun reveal(
        view: View,
        duration: Int,
        listener: AnimationListener?,
        center: Point?
    ): Animator {
        var center = center
        if (center == null) {
            center =
                getDefaultCenter(view)
        }
        val anim = ViewAnimationUtils.createCircularReveal(
            view,
            center.x,
            center.y,
            0f,
            getRevealRadius(
                center,
                view
            ).toFloat()
        )
        anim.addListener(object : DefaultActionAnimationListener(view, listener) {
            override fun defaultOnAnimationStart(view: View) {
                view.visibility = View.VISIBLE
            }
        })
        anim.duration = duration.toLong()
        anim.interpolator =
            defaultInterpolator
        return anim
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(view: View, duration: Int): Animator {
        return hide(
            view,
            duration,
            null,
            null
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(view: View, duration: Int, center: Point?): Animator {
        return hide(
            view,
            duration,
            null,
            center
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(
        view: View,
        duration: Int,
        listener: AnimationListener?
    ): Animator {
        return hide(
            view,
            duration,
            listener,
            null
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(view: View): Animator {
        return hide(
            view,
            ANIMATION_DURATION_DEFAULT
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(view: View, listener: AnimationListener?): Animator {
        return hide(
            view,
            ANIMATION_DURATION_DEFAULT,
            listener,
            null
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(view: View, center: Point?): Animator {
        return hide(
            view,
            ANIMATION_DURATION_DEFAULT,
            null,
            center
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(
        view: View,
        listener: AnimationListener?,
        center: Point?
    ): Animator {
        return hide(
            view,
            ANIMATION_DURATION_DEFAULT,
            listener,
            center
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun hide(
        view: View,
        duration: Int,
        listener: AnimationListener?,
        center: Point?
    ): Animator {
        var center = center
        if (center == null) {
            center =
                getDefaultCenter(view)
        }
        val anim = ViewAnimationUtils.createCircularReveal(
            view,
            center.x,
            center.y,
            getRevealRadius(
                center,
                view
            ).toFloat(),
            0f
        )
        anim.addListener(object : DefaultActionAnimationListener(view, listener) {
            override fun defaultOnAnimationEnd(view: View) {
                view.visibility = View.GONE
            }
        })
        anim.duration = duration.toLong()
        anim.interpolator =
            defaultInterpolator
        return anim
    }

    internal fun getDefaultCenter(view: View): Point {
        return Point(view.width / 2, view.height / 2)
    }

    internal fun getRevealRadius(center: Point, view: View): Int {
        var radius = 0f
        val points: MutableList<Point> =
            ArrayList()
        points.add(Point(view.left, view.top))
        points.add(Point(view.right, view.top))
        points.add(Point(view.left, view.bottom))
        points.add(Point(view.right, view.bottom))
        for (point in points) {
            val distance =
                distance(center, point)
            if (distance > radius) {
                radius = distance
            }
        }
        return Math.ceil(radius.toDouble()).toInt()
    }

    fun distance(first: Point, second: Point): Float {
        return Math.sqrt(
            Math.pow(
                first.x - second.x.toDouble(),
                2.0
            ) + Math.pow(first.y - second.y.toDouble(), 2.0)
        ).toFloat()
    }

    fun fadeIn(view: View, listener: AnimationListener?): Animator {
        return fadeIn(
            view,
            ANIMATION_DURATION_DEFAULT,
            listener
        )
    }

    @JvmOverloads
    fun fadeIn(
        view: View,
        duration: Int = ANIMATION_DURATION_DEFAULT,
        listener: AnimationListener? = null
    ): Animator {
        if (view.alpha == 1f) {
            view.alpha = 0f
        }
        val anim = ObjectAnimator.ofFloat(view, "alpha", 1f)
        anim.addListener(object : DefaultActionAnimationListener(view, listener) {
            override fun defaultOnAnimationStart(view: View) {
                view.visibility = View.VISIBLE
            }
        })
        anim.duration = duration.toLong()
        anim.interpolator =
            defaultInterpolator
        return anim
    }

    fun fadeOut(
        view: View,
        listener: AnimationListener?
    ): Animator {
        return fadeOut(
            view,
            ANIMATION_DURATION_DEFAULT,
            listener
        )
    }

    @JvmOverloads
    fun fadeOut(
        view: View,
        duration: Int = ANIMATION_DURATION_DEFAULT,
        listener: AnimationListener? = null
    ): Animator {
        val anim = ObjectAnimator.ofFloat(view, "alpha", 0f)
        anim.addListener(object : DefaultActionAnimationListener(view, listener) {
            override fun defaultOnAnimationEnd(view: View) {
                view.visibility = View.GONE
            }
        })
        anim.duration = duration.toLong()
        anim.interpolator =
            defaultInterpolator
        return anim
    }

    @JvmOverloads
    fun verticalSlideView(
        view: View,
        fromHeight: Int,
        toHeight: Int,
        listener: AnimationListener? = null
    ): Animator {
        return verticalSlideView(
            view,
            fromHeight,
            toHeight,
            ANIMATION_DURATION_DEFAULT,
            listener
        )
    }

    @JvmOverloads
    fun verticalSlideView(
        view: View,
        fromHeight: Int,
        toHeight: Int,
        duration: Int,
        listener: AnimationListener? = null
    ): Animator {
        val anim = ValueAnimator
            .ofInt(fromHeight, toHeight)
        anim.addUpdateListener { animation: ValueAnimator ->
            view.layoutParams.height = animation.animatedValue as Int
            view.requestLayout()
        }
        anim.addListener(
            DefaultActionAnimationListener(
                view,
                listener
            )
        )
        anim.duration = duration.toLong()
        anim.interpolator =
            defaultInterpolator
        return anim
    }

    internal val defaultInterpolator: Interpolator
        internal get() = FastOutSlowInInterpolator()

    interface AnimationListener {
        /**
         * @return return true to override the default behaviour
         */
        fun onAnimationStart(view: View): Boolean

        /**
         * @return return true to override the default behaviour
         */
        fun onAnimationEnd(view: View): Boolean

        /**
         * @return return true to override the default behaviour
         */
        fun onAnimationCancel(view: View): Boolean
    }

    private open class DefaultActionAnimationListener internal constructor(
        private val view: View,
        private val listener: AnimationListener?
    ) : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
            if (listener == null || !listener.onAnimationStart(view)) {
                defaultOnAnimationStart(view)
            }
        }

        override fun onAnimationEnd(animation: Animator) {
            if (listener == null || !listener.onAnimationEnd(view)) {
                defaultOnAnimationEnd(view)
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            if (listener == null || !listener.onAnimationCancel(view)) {
                defaultOnAnimationCancel(view)
            }
        }

        open fun defaultOnAnimationStart(view: View) { // No default action
        }

        open fun defaultOnAnimationEnd(view: View) { // No default action
        }

        fun defaultOnAnimationCancel(view: View) { // No default action
        }
    }
}
