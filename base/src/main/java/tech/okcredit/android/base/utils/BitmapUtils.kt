package tech.okcredit.android.base.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import io.reactivex.Single
import tech.okcredit.android.base.extensions.deleteIfExists
import tech.okcredit.android.base.extensions.makeRecursively
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

object BitmapUtils {
    const val IMAGE_QUALITY = 70
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable!!.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        bitmap?.let {
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
        return bitmap
    }

    fun getBitmapFromURL(imageUrl: String?, context: Context): Bitmap? {
        return try {
            Glide
                .with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()
                .get()
        } catch (e: InterruptedException) {
            null
        } catch (e: ExecutionException) {
            null
        }
    }

    fun Bitmap?.convertToCircle(): Bitmap? {
        if (this == null) {
            return null
        }
        val output = Bitmap.createBitmap(
            this.width,
            this.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color: Int = Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, this.width, this.height)
        val rectF = RectF(rect)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, rect, rect, paint)
        this.recycle()
        return output
    }

    fun textDrawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var width: Int = drawable.intrinsicWidth
        width = if (width > 0) width else 96 // Replaced the 1 by a 96
        var height: Int = drawable.intrinsicHeight
        height = if (height > 0) height else 96 // Replaced the 1 by a 96
        val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    fun getUriFromBitmap(
        bitmap: Bitmap,
        context: Context,
        folderName: String,
        imageName: String
    ): Single<Uri> {
        return Single.fromCallable {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, bytes)
            val storageDir = File(
                context.getExternalFilesDir(null),
                folderName
            )

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val fileName = UUID.randomUUID().toString() + imageName
            val resultImage = File(
                context.getExternalFilesDir(null),
                "$folderName/$fileName"
            )
            try {
                resultImage.makeRecursively()
                val fo = FileOutputStream(resultImage)
                fo.write(bytes.toByteArray())
                fo.close()
            } catch (e: IOException) {
                resultImage.deleteIfExists()
                throw e
            }
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                resultImage
            )
        }
            .onErrorReturn {
                throw it
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    fun getUriFromDrawable(context: Context, resourceId: Int): Uri {
        return Uri.parse("android.resource://" + context.getApplicationContext().getPackageName() + "/" + resourceId)
    }
}
