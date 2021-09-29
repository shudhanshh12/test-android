package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.views

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import tech.okcredit.user_migration.contract.models.AmountBox
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*
import kotlin.math.max

class RoboflowTransformer(
    private val width: Int,
    private val height: Int,
    private val amountBox: AmountBox,
    private val cropMargin: Int,
    private val strokeThickness: Int,
) : BitmapTransformation() {

    companion object {
        private const val VERSION = 2
        private const val ID = "in.okcredit.merchant.customer_ui.ui.add_txn_screen_old.views.RoboflowTransformer"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        val data = ByteBuffer.allocate(4 * 4 + 1)
            .put(VERSION.toByte())
            .putInt(amountBox.boxCoordinateX1 ?: 0)
            .putInt(amountBox.boxCoordinateX2 ?: 0)
            .putInt(amountBox.boxCoordinateY1 ?: 0)
            .putInt(amountBox.boxCoordinateY2 ?: 0)
            .array()
        messageDigest.update(data)
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int,
    ): Bitmap = run {
        val width = this.width.takeIf { it > 0 } ?: return@run null
        val height = this.height.takeIf { it > 0 } ?: return@run null

        val canvas = Canvas(toTransform)
        val scaleX = canvas.width.toFloat() / width
        val scaleY = canvas.height.toFloat() / height

        val boxCoordinateX1 = ((amountBox.boxCoordinateX1 ?: return@run null) * scaleX).toInt()
        val boxCoordinateX2 = ((amountBox.boxCoordinateX2 ?: return@run null) * scaleX).toInt()
        val boxCoordinateY1 = ((amountBox.boxCoordinateY1 ?: return@run null) * scaleY).toInt()
        val boxCoordinateY2 = ((amountBox.boxCoordinateY2 ?: return@run null) * scaleY).toInt()

        val cropMarginX = (cropMargin * scaleX).toInt()
        val cropMarginY = (cropMargin * scaleY).toInt()

        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeThickness.toFloat()
            color = Color.RED
        }

        canvas.drawRect(
            boxCoordinateX1.toFloat(),
            boxCoordinateY1.toFloat(),
            boxCoordinateX2.toFloat(),
            boxCoordinateY2.toFloat(),
            paint
        )

        val boxWidth = boxCoordinateX2 - boxCoordinateX1
        val boxHeight = boxCoordinateY2 - boxCoordinateY1
        val delta = max(boxWidth, boxHeight) / 2

        val centerX = boxCoordinateX1 + boxWidth / 2
        val centerY = boxCoordinateY1 + boxHeight / 2

        val startX = (centerX - delta - cropMarginX).coerceAtLeast(0)
        val startY = (centerY - delta - cropMarginY).coerceAtLeast(0)
        val endX = (centerX + delta + cropMarginX).coerceAtMost(canvas.width - 1)
        val endY = (centerY + delta + cropMarginY).coerceAtMost(canvas.height - 1)

        return@run runCatching {
            Bitmap.createBitmap(
                toTransform,
                startX,
                startY,
                endX - startX,
                endY - startY,
            )
        }.getOrNull()
    } ?: TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight)
}
