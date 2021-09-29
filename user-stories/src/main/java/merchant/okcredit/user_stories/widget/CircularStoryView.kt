package merchant.okcredit.user_stories.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import merchant.okcredit.user_stories.R
import timber.log.Timber

class CircularStoryView : AppCompatImageView {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    private var totalStories: Int = 0
    private var totalSeenStories: Int = 0
    private val oval = RectF()
    private var paint = Paint()

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return
        if (width == 0 || height == 0) {
            return
        }
        val b = (drawable as BitmapDrawable).bitmap
        val bitmap = b.copy(Bitmap.Config.ARGB_8888, true)
        val w = width
        val roundBitmap = getCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius: Float
        radius = if (width > height) {
            height / 2 - (BORDER_WIDTH / 2)
        } else {
            width / 2 - (BORDER_WIDTH / 2)
        }
        paint.isAntiAlias = true
        paint.strokeWidth = BORDER_WIDTH
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE
        val centerX = width / 2
        val centerY = height / 2
        val left = centerX - radius
        val top = centerY - radius
        val right = centerX + radius
        val bottom = centerY + radius
        oval[left, top, right] = bottom
        paint.color = ContextCompat.getColor(context, R.color.primary)
        var i = 0
        var totalSeen = totalSeenStories
        while (i < totalStories) {
            Timber.i("CircularStoryView $totalStories  $totalSeenStories $i")
            if (totalSeen > 0) {
                paint.color = ContextCompat.getColor(context, R.color.grey300)
                totalSeen--
            } else {
                paint.color = ContextCompat.getColor(context, R.color.primary)
            }

            val storySize = totalStories
            val storyGap = if (storySize == 1) 0 else STORIES_GAP_ANGLE_NORMAL
            val startAngle = -90 + i * (360 / storySize.toFloat()) + storyGap
            val sweepAngle = 360 / storySize.toFloat() - 2 * storyGap

            canvas.drawArc(
                oval,
                startAngle,
                sweepAngle,
                false,
                paint
            )
            i++
        }
    }

    fun setData(totalStories: Int = 1, totalSeenStories: Int = 0) {
        this.totalStories = totalStories
        this.totalSeenStories = totalSeenStories
        invalidate()
    }

    companion object {

        const val BORDER_WIDTH = 4f
        const val STORIES_GAP_ANGLE_NORMAL = 5

        fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
            val sBmp: Bitmap
            sBmp = if (bmp.width != radius || bmp.height != radius) {
                val smallest = bmp.width.coerceAtMost(bmp.height).toFloat()
                val factor = smallest / radius
                Bitmap.createScaledBitmap(bmp, (bmp.width / factor).toInt(), (bmp.height / factor).toInt(), false)
            } else {
                bmp
            }
            val output = Bitmap.createBitmap(
                radius, radius,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            val paint = Paint()
            val rect = Rect(0, 0, radius, radius)
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(
                radius / 2.toFloat(),
                radius / 2.toFloat(), radius / 2 - BORDER_WIDTH - 4, paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(sBmp, rect, rect, paint)
            return output
        }
    }
}
