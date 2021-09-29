package `in`.okcredit.backend._offline.usecase

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import io.reactivex.Single
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import javax.inject.Inject

class GetWhatsAppShareIntent @Inject constructor(
    val context: Context,
    val communicationApi: CommunicationRepository
) {

    companion object {
        const val FOLDER_NAME = "reminder_images"
        const val FILE_NAME = "reminder_image.jpg"
    }

    fun execute(req: WhatsAppShareRequest): Single<Intent> {
        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = req.shareText,
            phoneNumber = req.mobile,
            imageFrom = ImagePath.ImageUriFromBitMap(
                req.bitmap ?: BitmapFactory.decodeResource(
                    context.resources,
                    req.drawableImage
                ),
                context, FOLDER_NAME, FILE_NAME
            )

        )
        return communicationApi.goToWhatsApp(whatsappIntentBuilder)
    }

    data class WhatsAppShareRequest(
        val shareText: String?,
        val mobile: String?,
        val bitmap: Bitmap? = null,
        val screen: String? = null,
        @DrawableRes val drawableImage: Int = 0
    )
}
