package `in`.okcredit.shared.performance.app_startup

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver

class NextDrawListener(
    val view: View,
    val onDrawCallback: () -> Unit
) : ViewTreeObserver.OnDrawListener {

    private val handler = Handler(Looper.getMainLooper())
    var invoked = false

    override fun onDraw() {
        if (invoked) return
        invoked = true
        onDrawCallback()
        handler.post {
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnDrawListener(this)
            }
        }
    }

    companion object {
        fun View.onNextDraw(onDrawCallback: () -> Unit) {
            if (viewTreeObserver.isAlive && isAttachedToWindow) {
                addNextDrawListener(onDrawCallback)
            } else {
                addOnAttachStateChangeListener(
                    object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            addNextDrawListener(onDrawCallback)
                            removeOnAttachStateChangeListener(this)
                        }

                        override fun onViewDetachedFromWindow(v: View) = Unit
                    })
            }
        }

        internal fun View.addNextDrawListener(callback: () -> Unit) {
            viewTreeObserver.addOnDrawListener(
                NextDrawListener(this, callback)
            )
        }
    }
}
