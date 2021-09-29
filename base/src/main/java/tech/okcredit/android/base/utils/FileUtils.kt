package tech.okcredit.android.base.utils

import `in`.okcredit.shared.service.rxdownloader.RxDownloader
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.extensions.makeIfNotExists
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.nio.channels.FileChannel
import javax.inject.Inject

class FileUtils @Inject constructor(
    private val context: Lazy<Context>,
    private val rxDownloader: Lazy<RxDownloader>,
) {

    companion object {
        @Throws(IOException::class)
        fun copyFile(sourceFile: File?, destinationFile: File) {
            make(destinationFile)
            var source: FileChannel? = null
            var destination: FileChannel? = null
            try {
                source = FileInputStream(sourceFile).channel
                destination = FileOutputStream(destinationFile).channel
                destination.transferFrom(source, 0, source.size())
            } finally {
                source?.close()
                destination?.close()
            }
        }

        fun make(file: File) {
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            if (!file.exists()) {
                file.createNewFile()
            }
        }

        fun getImageUriFromFile(
            file: File,
            context: Context
        ): Single<Uri> {
            return Single.fromCallable {
                if (file.exists())
                    FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
                else
                    throw FileNotFoundException()
            }
        }

        fun getPdfUri(
            context: Context,
            fileUrl: String,
            destinationPath: String,
            fileName: String,
            isCompletedNotificationShow: Boolean = false,
            directoryName: String = "share"
        ): Single<Uri> {
            val storageDir = File(context.getExternalFilesDir(null), directoryName)
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val rxDownloader = RxDownloader(context)
            return rxDownloader
                .downloadInFilesDir(
                    fileUrl, fileName, destinationPath,
                    "application/pdf", isCompletedNotificationShow
                )
                .doOnSuccess { s -> Timber.d("download successful") }
                .doOnError { throwable1 ->
                    Timber.e(throwable1, "Download Failed")
                    throw throwable1
                }
                .map { path -> File(URI(path)) }
                .map { file ->
                    return@map FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        file
                    )
                }
                .subscribeOn(ThreadUtils.newThread())
        }

        fun getLocalFile(context: Context, folderName: String, fileName: String): File {
            return File(context.getExternalFilesDir(null), "$folderName/$fileName")
        }
    }

    fun getImageUriFromRemote(
        file: File,
        localFolderName: String,
        localFileName: String,
        remoteUrl: String
    ): Single<Uri> {
        return if (file.exists()) {
            Single.just(
                FileProvider.getUriForFile(context.get(), context.get().packageName + ".provider", file)
            )
        } else {
            val storageDir = File(context.get().getExternalFilesDir(null), localFolderName)
            storageDir.makeIfNotExists()

            return rxDownloader.get().downloadInFilesDir(
                remoteUrl,
                localFileName,
                localFolderName,
                "image/jpeg",
                false
            ).subscribeOn(ThreadUtils.newThread())
                .doOnSuccess {
                    Timber.d("<<download successful!")
                }.map {
                    FileProvider.getUriForFile(
                        context.get(),
                        context.get().packageName + ".provider",
                        File(URI(it))
                    )
                }
        }
    }
}
