package `in`.okcredit.shared.performance.layout_perf

import `in`.okcredit.shared.R
import `in`.okcredit.shared.performance.PerformanceTracker
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ScrollView
import dagger.Lazy
import timber.log.Timber

@SuppressLint("CustomViewStyleable")
class ScrollViewLayoutTracker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private var layoutPerformanceTracker: Lazy<PerformanceTracker>? = null

    private var tag: String
    private var layoutPerformanceData: LayoutPerformanceData = LayoutPerformanceData()

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.LayoutTracker, defStyleAttr, 0)
        val layoutName = attributes.getString(R.styleable.LayoutTracker_layoutName) ?: ""
        val isRecycler = attributes.getBoolean(R.styleable.LayoutTracker_isRecycler, false)
        attributes.recycle()
        layoutPerformanceData.layoutName = layoutName

        tag = "<<<<<<ScrollView $layoutName"
        Timber.d("$tag Init")

        layoutPerformanceData.shouldTrackThisView(isRecycler)
    }

    fun setTracker(layoutPerformanceTracker: Lazy<PerformanceTracker>) {
        this.layoutPerformanceTracker = layoutPerformanceTracker
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        layoutPerformanceData.logOnDetachedFromWindow(tag, layoutPerformanceTracker)
    }

    override fun onDraw(canvas: Canvas) {
        layoutPerformanceData.logOnDraw(tag) {
            super.onDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        layoutPerformanceData.logOnMeasure(tag) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        layoutPerformanceData.logOnLayout(tag) {
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutPerformanceData.logOnSizeChanged(tag)
    }
}
