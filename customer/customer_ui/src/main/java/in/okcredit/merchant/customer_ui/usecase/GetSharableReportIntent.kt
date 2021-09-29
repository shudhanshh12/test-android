package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.content.Intent
import io.reactivex.Single
import org.joda.time.DateTime
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import javax.inject.Inject

class GetSharableReportIntent @Inject constructor(
    val context: Context,
    private val communicationApi: CommunicationRepository,
) {

    companion object {
        const val DESTINATION_PATH = "share"
        const val URI_TYPE = "application/pdf"
    }

    fun execute(phNo: String?, fileUrl: String, fileName: String): Single<Intent> {
        val shareText = context.getString(R.string.share_msg)
        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = shareText,
            phoneNumber = phNo,
            imageFrom = ImagePath.PdfUriFromRemote(
                context = context,
                fileUrl = fileUrl,
                destinationPath = DESTINATION_PATH,
                fileName = fileName
            ),
            uriType = URI_TYPE
        )

        return communicationApi.goToWhatsApp(whatsappIntentBuilder).onErrorResumeNext {
            if (it is IntentHelper.NoWhatsAppError)
                return@onErrorResumeNext communicationApi.goToSharableApp(whatsappIntentBuilder)
            else
                return@onErrorResumeNext Single.error(it)
        }
    }

    data class Request(val customerId: String, val type: String, val startDate: DateTime, val endDate: DateTime)
}
