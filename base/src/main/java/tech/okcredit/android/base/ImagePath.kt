package tech.okcredit.android.base

import android.content.Context
import android.graphics.Bitmap
import java.io.File

sealed class ImagePath {
    data class ImageUriFromLocal(
        val context: Context,
        val localFolderName: String,
        val fileName: String,
        val file: File
    ) : ImagePath()

    data class ImageUriFromBitMap(
        val bitmap: Bitmap,
        val context: Context,
        val folderName: String,
        val imageName: String
    ) : ImagePath()

    data class ImageUriFromRemote(
        val file: File,
        val localFolderName: String,
        val fileUrl: String,
        val localFileName: String
    ) : ImagePath()

    data class PdfUriFromRemote(
        val context: Context,
        val fileUrl: String,
        val destinationPath: String,
        val fileName: String
    ) : ImagePath()
}
