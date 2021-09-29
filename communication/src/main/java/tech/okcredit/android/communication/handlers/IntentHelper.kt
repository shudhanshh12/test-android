package tech.okcredit.android.communication.handlers

import `in`.okcredit.merchant.device.DeviceUtils
import `in`.okcredit.merchant.device.DeviceUtils.Companion.WHATSAPP_BUSINESS_PACKAGE_NAME
import `in`.okcredit.merchant.device.DeviceUtils.Companion.WHATSAPP_PACKAGE_NAME
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.utils.BitmapUtils
import tech.okcredit.android.base.utils.FileUtils
import tech.okcredit.android.communication.R
import tech.okcredit.android.communication.ShareIntentBuilder
import java.util.*
import javax.inject.Inject

class IntentHelper @Inject constructor(
    private val context: Lazy<Context>,
    private val deviceUtils: Lazy<DeviceUtils>,
    private val fileUtils: Lazy<FileUtils>
) {

    companion object {

        private fun whatsappIntentOnlyForText(shareIntentBuilder: ShareIntentBuilder): Intent {
            var uriString = if (shareIntentBuilder.phoneNumber != null) {
                "https://wa.me/91${shareIntentBuilder.phoneNumber}?text=${shareIntentBuilder.shareText}"
            } else {
                "https://wa.me/?text=${shareIntentBuilder.shareText}"
            }
            val uri: Uri = Uri.parse(uriString)
            return Intent(Intent.ACTION_VIEW, uri)
        }
    }

    fun getWhatsAppIntent(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        return Single.create { single ->
            if (deviceUtils.get().isWhatsAppInstalled() || deviceUtils.get().isWhatsAppBusinessInstalled()) {
                val whatsAppIntent = Intent("android.intent.action.MAIN")
                val whatsAppBusinessIntent = Intent("android.intent.action.MAIN")

                whatsAppIntent.action = Intent.ACTION_SEND
                whatsAppBusinessIntent.action = Intent.ACTION_SEND

                whatsAppIntent.setPackage(WHATSAPP_PACKAGE_NAME)
                whatsAppBusinessIntent.setPackage(WHATSAPP_BUSINESS_PACKAGE_NAME)

                if (shareIntentBuilder.shareText != null) {
                    whatsAppIntent.putExtra(Intent.EXTRA_TEXT, shareIntentBuilder.shareText)
                    whatsAppBusinessIntent.putExtra(Intent.EXTRA_TEXT, shareIntentBuilder.shareText)

                    whatsAppIntent.type = "text/plain"
                    whatsAppBusinessIntent.type = "text/plain"
                }

                if (shareIntentBuilder.uri != null) {
                    whatsAppIntent.putExtra(Intent.EXTRA_STREAM, shareIntentBuilder.uri)
                    whatsAppBusinessIntent.putExtra(Intent.EXTRA_STREAM, shareIntentBuilder.uri)

                    whatsAppBusinessIntent.type = shareIntentBuilder.uriType
                    whatsAppIntent.type = shareIntentBuilder.uriType
                } else {
                    single.onSuccess(whatsappIntentOnlyForText(shareIntentBuilder))
                }
                if (shareIntentBuilder.phoneNumber != null) {
                    whatsAppIntent.putExtra("jid", "91${shareIntentBuilder.phoneNumber}@s.whatsapp.net")
                    whatsAppBusinessIntent.putExtra(
                        "phoneNumber",
                        "91${shareIntentBuilder.phoneNumber}@s.whatsapp.net"
                    )
                }

                single.onSuccess(returnWhatsAppIntent(whatsAppIntent, whatsAppBusinessIntent))
            } else {
                single.onError(NoWhatsAppError())
            }
        }
    }

    // As whatsappIntentOnlyForText() was not able to handle use case where a number not added to whatsapp

    fun getWhatsAppIntentForTextOnlyWithExtendedBehaviour(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        return Single.create { single ->
            if (deviceUtils.get().isWhatsAppInstalled() || deviceUtils.get().isWhatsAppBusinessInstalled()) {
                val whatsAppIntent = Intent("android.intent.action.MAIN")
                val whatsAppBusinessIntent = Intent("android.intent.action.MAIN")

                whatsAppIntent.action = Intent.ACTION_SEND
                whatsAppBusinessIntent.action = Intent.ACTION_SEND

                whatsAppIntent.setPackage(WHATSAPP_PACKAGE_NAME)
                whatsAppBusinessIntent.setPackage(WHATSAPP_BUSINESS_PACKAGE_NAME)

                if (shareIntentBuilder.shareText != null) {
                    whatsAppIntent.putExtra(Intent.EXTRA_TEXT, shareIntentBuilder.shareText)
                    whatsAppBusinessIntent.putExtra(Intent.EXTRA_TEXT, shareIntentBuilder.shareText)

                    whatsAppIntent.type = "text/plain"
                    whatsAppBusinessIntent.type = "text/plain"
                }

                if (shareIntentBuilder.phoneNumber != null) {
                    whatsAppIntent.putExtra("jid", "91${shareIntentBuilder.phoneNumber}@s.whatsapp.net")
                    whatsAppBusinessIntent.putExtra(
                        "phoneNumber",
                        "91${shareIntentBuilder.phoneNumber}@s.whatsapp.net"
                    )
                }

                single.onSuccess(returnWhatsAppIntent(whatsAppIntent, whatsAppBusinessIntent))
            } else {
                single.onError(NoWhatsAppError())
            }
        }
    }

    private fun returnWhatsAppIntent(
        whatsAppIntent: Intent,
        whatsAppBusinessIntent: Intent
    ): Intent {
        if (deviceUtils.get().isWhatsAppInstalled() && !deviceUtils.get().isWhatsAppBusinessInstalled()) {
            return whatsAppIntent
        }
        if (deviceUtils.get().isWhatsAppBusinessInstalled() && !deviceUtils.get().isWhatsAppInstalled()) {
            return whatsAppBusinessIntent
        }
        if (deviceUtils.get().isWhatsAppBusinessInstalled() && deviceUtils.get().isWhatsAppInstalled()) {
            val targetShareIntents = ArrayList<Intent>()
            targetShareIntents.add(whatsAppIntent)
            targetShareIntents.add(whatsAppBusinessIntent)
            val chooseIntent =
                Intent.createChooser(
                    targetShareIntents.removeAt(0),
                    context.get().getString(R.string.select_the_whatsapp_to_share)
                )
            chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toTypedArray<Parcelable>())
            return chooseIntent
        } else {
            return Intent().setPackage(null)
        }
    }

    fun checkUri(imageFrom: ImagePath?): Single<Uri> {
        return when (imageFrom) {
            is ImagePath.ImageUriFromBitMap -> {
                return BitmapUtils.getUriFromBitmap(
                    bitmap = imageFrom.bitmap,
                    context = imageFrom.context,
                    folderName = imageFrom.folderName,
                    imageName = imageFrom.imageName
                )
            }
            is ImagePath.ImageUriFromLocal -> {
                return FileUtils.getImageUriFromFile(
                    file = imageFrom.file,
                    context = imageFrom.context
                )
            }
            is ImagePath.ImageUriFromRemote -> {
                return fileUtils.get().getImageUriFromRemote(
                    file = imageFrom.file,
                    localFolderName = imageFrom.localFolderName,
                    localFileName = imageFrom.localFileName,
                    remoteUrl = imageFrom.fileUrl
                )
            }
            is ImagePath.PdfUriFromRemote -> {
                return FileUtils.getPdfUri(
                    context = imageFrom.context,
                    fileUrl = imageFrom.fileUrl,
                    destinationPath = imageFrom.destinationPath,
                    fileName = imageFrom.fileName
                )
            }
            else -> Single.just(null)
        }
    }

    fun getAllIntent(shareIntentBuilder: ShareIntentBuilder): Single<Intent> {
        var resultIntent = Intent()
        return Single.create { single ->
            if (shareIntentBuilder.contentType != null) {
                resultIntent.putExtra("content_type", shareIntentBuilder.contentType)
            }
            if (shareIntentBuilder.shareText != null)
                resultIntent.putExtra(Intent.EXTRA_TEXT, shareIntentBuilder.shareText)
            if (shareIntentBuilder.uri != null) {
                resultIntent.putExtra(Intent.EXTRA_STREAM, shareIntentBuilder.uri)
            }
            resultIntent.type = shareIntentBuilder.uriType
            resultIntent.action = Intent.ACTION_SEND
            resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            resultIntent = Intent.createChooser(
                resultIntent,
                context.get().getString(R.string.select_the_app_to_be_shared_on)
            )
            single.onSuccess(resultIntent)
        }
    }

    class NoWhatsAppError : Exception()
}
