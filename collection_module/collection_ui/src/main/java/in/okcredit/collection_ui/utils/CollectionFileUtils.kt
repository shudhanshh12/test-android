package `in`.okcredit.collection_ui.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object CollectionFileUtils {

    @RequiresApi(Build.VERSION_CODES.Q)
    @Throws(IOException::class)
    fun saveBitmap(
        context: Context,
        @NonNull bitmap: Bitmap,
        format: CompressFormat,
        @NonNull mimeType: String,
        fileName: String,
        filePath: String? = null
    ) {
        val relativePath = filePath ?: Environment.DIRECTORY_PICTURES + File.separator + "OkCredit"

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        val resolver: ContentResolver = context.contentResolver

        var stream: OutputStream? = null
        var uri: Uri? = null
        try {
            val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)

            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }

            stream = resolver.openOutputStream(uri)
            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }

            if (!bitmap.compress(format, 95, stream)) {
                throw IOException("Failed to save bitmap.")
            }
        } catch (e: IOException) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null)
            }
            Timber.e("Exception : ${e.stackTrace}")
            ExceptionUtils.logException(Exception("Unable to save merchant QR", e))
            throw e
        } finally {
            stream?.close()
        }
    }

    fun saveBitmapPreQ(context: Context, bitmap: Bitmap, fileName: String) {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 90, bytes)

        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "OkCredit"
        )

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val outputFile = File(storageDir, fileName)

        try {
            val fo = FileOutputStream(outputFile)
            fo.write(bytes.toByteArray())
            fo.close()

            MediaScannerConnection.scanFile(
                context,
                arrayOf(outputFile.toString()), null
            ) { path, uri -> Timber.i("Scan completed for $path and uri $uri") }
        } catch (e: IOException) {
            Timber.e("Exception : ${e.stackTrace}")
            ExceptionUtils.logException(Exception("Unable to save merchant QR", e))
        }
    }
}
