package tech.okcredit.android.base.utils

import android.content.Context
import android.net.Uri
import java.io.File
import javax.inject.Inject

class ImageCache @Inject constructor(private val context: Context) {

    fun getImage(url: String?): String? {
        return if (url.isNullOrEmpty()) {
            url
        } else {
            val lastPath = Uri.parse(url).lastPathSegment ?: return url
            val localCopy = File(getAwsStorageDir(), lastPath)
            if (localCopy.exists()) {
                localCopy.absolutePath
            } else {
                url
            }
        }
    }

    private fun getAwsStorageDir(): File? {
        val storageDir = File(context.filesDir, "aws-storage")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return storageDir
    }
}
