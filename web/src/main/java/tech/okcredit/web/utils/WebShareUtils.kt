package tech.okcredit.web.utils

import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.shared.service.rxdownloader.RxDownloader
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.io.File
import java.net.URI
import java.util.*

object WebShareUtils {
    fun getWhatsAppIntent(
        shareText: String?,
        phoneNumber: String?,
        url: String?,
        context: Context,
        deviceUtils: DeviceUtils,
    ): Single<Intent> {

        var fileDownloadTask = Single.just(File(""))
        if (url.isNullOrEmpty().not()) {
            val rxDownloader = RxDownloader(context)
            fileDownloadTask = rxDownloader
                .downloadInFilesDir(
                    url!!, UUID.randomUUID().toString() + ".jpg", "share",
                    "image/jpeg", false
                )
                .doOnSuccess {
                    Timber.d(
                        "download successful"
                    )
                }
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                        Timber.e(throwable, "Download Failed")
                    }
                }
                .map { path: String? ->
                    File(URI(path))
                }
        }

        return fileDownloadTask
            .flatMap { file: File ->
                var sendIntent = Intent()
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.type = "text/plain"

                val appInstalled = deviceUtils.isWhatsAppInstalled()
                val packageName = "com.whatsapp"

                if (appInstalled) {
                    // Adding image if file exist
                    if (file.exists()) {
                        val shareUri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            file
                        )
                        sendIntent.type = "imageLocal/jpg"
                        sendIntent.putExtra(Intent.EXTRA_STREAM, shareUri)

                        sendIntent.setPackage(packageName)
                        if (phoneNumber.isNullOrEmpty().not()) {
                            sendIntent.component = ComponentName("com.whatsapp", "com.whatsapp.Conversation")
                            sendIntent.putExtra("jid", "91$phoneNumber@s.whatsapp.net")
                            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                    } else {
                        val uriBuilder: Uri.Builder = Uri.parse("whatsapp://send")
                            .buildUpon()
                            .appendQueryParameter("text", shareText)

                        val uri: Uri = when {
                            phoneNumber?.isEmpty() ?: true -> uriBuilder
                            else -> uriBuilder.appendQueryParameter("phone", ("91$phoneNumber"))
                        }.build()

                        val intent: Intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(uri)

                        sendIntent = intent
                    }
                }
                return@flatMap Single.just(sendIntent)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    fun getGeneralShareIntent(
        shareText: String?,
        imageUri: String?,
        context: Context,
    ): Single<Intent> {
        var fileDownloadTask = Single.just(File(""))
        if (imageUri.isNullOrEmpty().not()) {
            val rxDownloader = RxDownloader(context)
            fileDownloadTask = rxDownloader
                .downloadInFilesDir(
                    imageUri!!, UUID.randomUUID().toString() + ".jpg", "share",
                    "image/jpeg", false
                )
                .doOnSuccess {
                    Timber.d(
                        "download successful"
                    )
                }
                .doOnError { throwable: Throwable? ->
                    throwable?.let {
                        RecordException.recordException(it)
                        Timber.e(throwable, "Download Failed")
                    }
                }
                .map { path: String? ->
                    File(URI(path))
                }
        }

        return fileDownloadTask
            .flatMap { file: File ->
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                if (file.exists()) {
                    val shareUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        file
                    )
                    shareIntent.type = "imageLocal/jpg"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri)
                }
                return@flatMap Single.just(shareIntent)
            }
            .subscribeOn(ThreadUtils.newThread())
    }
}
