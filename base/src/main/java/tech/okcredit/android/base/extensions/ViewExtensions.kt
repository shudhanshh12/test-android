package tech.okcredit.android.base.extensions

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import tech.okcredit.android.base.utils.release

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.getString(@StringRes stringResId: Int): String = resources.getString(stringResId)

fun View.getColorDrawable(@ColorRes colorId: Int) = context.getColorDrawable(colorId)

fun View.snackbar(text: CharSequence, duration: Int): Snackbar =
    Snackbar.make(
        this,
        Html.fromHtml("<font color=\"#ffffff\">$text</font>"),
        duration
    )

fun Group.setGroupOnClickListener(listener: (view: View) -> Unit) {
    val parent = this.parent
    if (parent != null && parent is ConstraintLayout) {
        referencedIds.forEach {
            val view = parent.getViewById(it)
            view.setOnClickListener(listener)
        }
    }
}

fun Group.alpha(value: Float) {
    val parent = this.parent
    if (parent != null && parent is ConstraintLayout) {
        referencedIds.forEach {
            val view = parent.getViewById(it)
            view.alpha = value
        }
    }
}

fun Group.animateAlpha(value: Float, duration: Long = 300L) {
    val parent = this.parent
    if (parent != null && parent is ConstraintLayout) {
        referencedIds.forEach {
            val view = parent.getViewById(it)
            view.animate().alpha(value).duration = duration
        }
    }
}

fun View.topSnackbar(text: CharSequence, duration: Int): Snackbar {
    val snackbar = Snackbar.make(this, Html.fromHtml("<font color=\"#000000\">$text</font>"), duration)
    val snackbarLayout = snackbar.view
    val lp = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    lp.setMargins(20, 120, 20, 0)
    snackbarLayout.layoutParams = lp
    snackbarLayout.setBackgroundColor(Color.WHITE)
    return snackbar
}

fun SpannableStringBuilder.withClickableSpan(
    start: Int,
    end: Int,
    onClickListener: () -> Unit,
): SpannableStringBuilder {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) = onClickListener.invoke()
    }
    setSpan(
        clickableSpan,
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

@Deprecated(message = "Use core ktx instead. isVisible = true/false")
fun View.setBooleanVisibility(value: Boolean) {
    if (value) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.convertDateToMillis(givenFormat: String, givenDate: String): Long {
    return DateTimeFormat.forPattern(givenFormat).parseMillis(givenDate)
}

fun AppCompatEditText.afterTextChange(afterTextChange: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChange(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun View.isVisible() = visibility == View.VISIBLE

fun ImageView.greyOut() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0F)
    val cf = ColorMatrixColorFilter(matrix)
    this.colorFilter = cf
    this.imageAlpha = 128
}

fun ViewGroup.inflate(@LayoutRes layout: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this.context).inflate(
        layout,
        this,
        attachToRoot
    )

inline fun <reified T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun EditText.disableCopyPaste() {
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
    isLongClickable = false
    setTextIsSelectable(false)
}

fun View.translateY(duration: Int, distance: Float) {

    val animator = ObjectAnimator.ofFloat(this, "translationY", distance)
    animator.duration = duration.toLong()
    animator.interpolator = LinearInterpolator()
    animator.start()
}

fun Window.disableScreanCapture() {
    release {
        setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }
}

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}

fun MotionLayout.setViewVisibility(@IdRes viewId: Int, visibility: Int) {
    this.constraintSetIds.forEach {
        getConstraintSet(it).setVisibility(viewId, visibility)
    }
}

fun View.debounceClickListener(
    delayMillis: Long = 300L,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
    block: () -> Unit,
) {
    var debounceJob: Job? = null
    this.setOnClickListener {
        if (debounceJob == null) {
            debounceJob = scope.launch {
                block()
                delay(delayMillis)
                debounceJob = null
            }
        }
    }
}
