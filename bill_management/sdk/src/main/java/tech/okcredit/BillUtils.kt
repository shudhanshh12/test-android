package tech.okcredit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Environment
import android.view.View
import kotlinx.coroutines.*
import tech.okcredit.camera_contract.CapturedImage
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

object BillUtils {

    const val contrastVal = "1.2"
    private const val brightnessVal = "3"
    fun generateRandomId() = UUID.randomUUID().toString()
    fun currentTimestamp() = System.currentTimeMillis()
    suspend fun enhanceImages(
        listPhotos: ArrayList<CapturedImage>,
        coroutineScope: CoroutineScope
    ): ArrayList<CapturedImage> {
        val list = ArrayList<CapturedImage>()
        val deferred = mutableListOf<Deferred<Boolean>>()
        listPhotos.forEach {
            deferred.add(
                coroutineScope.async(Dispatchers.Default) {
                    val bitmap: Bitmap = BitmapFactory.decodeFile(it.file.path)
                    val enhancedBitmap = enhanceImage(bitmap, contrastVal.toFloat(), brightnessVal.toFloat())
                    val file = saveBitmapToFile(enhancedBitmap!!)
                    list.add(CapturedImage(file))
                }
            )
        }

        deferred.awaitAll()
        return list
    }

    object SCREEN_SOURCE {
        const val MULTI_IMAGE_ACTIVITY = "multi_image_activity"
    }

    fun enhanceImage(
        mBitmap: Bitmap,
        contrast: Float = contrastVal.toFloat(),
        brightness: Float = brightnessVal.toFloat()
    ): Bitmap {
        val cm = ColorMatrix(
            floatArrayOf(
                contrast,
                0f,
                0f,
                0f,
                brightness,
                0f,
                contrast,
                0f,
                0f,
                brightness,
                0f,
                0f,
                contrast,
                0f,
                brightness,
                0f,
                0f,
                0f,
                1f,
                0f
            )
        )
        val mEnhancedBitmap: Bitmap = Bitmap.createBitmap(
            mBitmap.width, mBitmap.height,
            mBitmap
                .config
        )
        val canvas = Canvas(mEnhancedBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, paint)
        return mEnhancedBitmap
    }

    fun saveBitmapToFile(enhancedBitmap: Bitmap): File {
        val filePath: String = Environment.getExternalStorageDirectory().getAbsolutePath().toString() +
            "/Okcredit"
        val dir = File(filePath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "bills" + generateRandomId() + ".png")
        val fOut = FileOutputStream(file)

        enhancedBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()
        return file
    }

    /** Combination of all flags required to put activity into immersive mode */
    const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
}
