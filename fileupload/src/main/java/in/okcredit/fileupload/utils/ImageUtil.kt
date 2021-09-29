package `in`.okcredit.fileupload.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.*

object ImageUtil {
    @Throws(IOException::class)
    fun correctOrientation(image: File): Bitmap {
        if (!image.exists())
            throw FileNotFoundException()

        val exif = android.media.ExifInterface(image.toURI().path)
        val rotation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)
        val rotationInDegrees = exifToDegrees(rotation)

        val srcBmp = BitmapFactory.decodeFile(image.absolutePath)
        val matrix = Matrix()
        if (rotation.toFloat() != 0f)
            matrix.preRotate(rotationInDegrees.toFloat())
        return Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.width, srcBmp.height, matrix, true)
    }

    fun scale(srcBmp: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val scale = Math.min(maxHeight.toFloat() / srcBmp.width, maxWidth.toFloat() / srcBmp.height)
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val scaled = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.width, srcBmp.height, matrix, true)
        srcBmp.recycle()
        return scaled
    }

    fun compressedStream(srcBmp: Bitmap, quality: Int): InputStream {
        val byteOutStream = ByteArrayOutputStream()
        srcBmp.compress(Bitmap.CompressFormat.JPEG, quality, byteOutStream)
        return ByteArrayInputStream(byteOutStream.toByteArray())
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == android.media.ExifInterface.ORIENTATION_ROTATE_90)
            return 90
        else if (exifOrientation == android.media.ExifInterface.ORIENTATION_ROTATE_180)
            return 180
        else if (exifOrientation == android.media.ExifInterface.ORIENTATION_ROTATE_270)
            return 270
        return 0
    }
}
