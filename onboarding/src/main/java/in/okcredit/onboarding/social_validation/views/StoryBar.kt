package `in`.okcredit.onboarding.social_validation.views

import `in`.okcredit.onboarding.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class StoryBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var count: Int = 0
        private set
    var activeIndex: Int = 0
        private set
    var percent: Int = 0
        private set

    private var storyLengthX: Float = 0f
    private var midlineY: Float = 0f
    private val spacing = resources.getDimension(R.dimen._4dp)

    private val foregroundPaint = Paint().apply {
        this.color = ContextCompat.getColor(context, R.color.primary)
        this.strokeWidth = resources.getDimension(R.dimen._2dp)
        this.strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint = Paint().apply {
        this.color = ContextCompat.getColor(context, R.color.transparent_grey)
        this.strokeWidth = resources.getDimension(R.dimen._2dp)
        this.strokeCap = Paint.Cap.ROUND
    }

    fun setProgress(count: Int, activeIndex: Int, percent: Int) {
        this.count = count
        this.activeIndex = activeIndex
        this.percent = percent

        this.storyLengthX = (measuredWidth - (count - 1) * spacing) / count
        this.midlineY = measuredHeight.toFloat() / 2

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        (0 until count).forEach {
            when {
                it < activeIndex -> drawCompeted(canvas, it)
                it == activeIndex -> drawPartial(canvas, it)
                it > activeIndex -> drawTrack(canvas, it)
            }
        }
    }

    private fun drawCompeted(canvas: Canvas, it: Int) {
        val startX = getStartCoordinate(it)
        val endX = getEndCoordinate(it)

        canvas.drawLine(startX, midlineY, endX, midlineY, foregroundPaint)
    }

    private fun drawPartial(canvas: Canvas, it: Int) {
        drawTrack(canvas, it)

        val startX = getStartCoordinate(it)
        val endX = startX + (storyLengthX * percent) / 100

        canvas.drawLine(startX, midlineY, endX, midlineY, foregroundPaint)
    }

    private fun drawTrack(canvas: Canvas, it: Int) {
        val startX = getStartCoordinate(it)
        val endX = getEndCoordinate(it)

        canvas.drawLine(startX, midlineY, endX, midlineY, backgroundPaint)
    }

    private fun getStartCoordinate(it: Int): Float {
        return it * (storyLengthX + spacing)
    }

    private fun getEndCoordinate(it: Int): Float {
        return getStartCoordinate(it) + storyLengthX
    }
}
