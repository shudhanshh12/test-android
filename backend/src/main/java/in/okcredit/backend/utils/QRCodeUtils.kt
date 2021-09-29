package `in`.okcredit.backend.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.TypedValue
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.*

object QRCodeUtils {

    @Throws(WriterException::class)
    fun encodeAsBitmap(str: String, context: Context, width: Int): Bitmap? {
        val result: BitMatrix
        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints.put(EncodeHintType.MARGIN, 0)
            result = MultiFormatWriter().encode(
                str,
                BarcodeFormat.QR_CODE,
                dp2px(context, width.toFloat()).toInt(),
                dp2px(context, width.toFloat()).toInt(),
                hints
            )
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

    fun dp2px(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    fun getBitmapObservable(str: String, context: Context, width: Int): Observable<Bitmap?> =
        Observable.fromCallable {
            encodeAsBitmap(str, context, width)
        }.subscribeOn(ThreadUtils.computation())
            .observeOn(AndroidSchedulers.mainThread())

    fun getBitmap(str: String, context: Context, width: Int): Bitmap? =
        encodeAsBitmap(str, context, width)
}
