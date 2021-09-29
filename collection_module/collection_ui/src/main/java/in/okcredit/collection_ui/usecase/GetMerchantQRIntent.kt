package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import android.content.Intent
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import timber.log.Timber
import javax.inject.Inject

class GetMerchantQRIntent @Inject constructor(
    val context: Context,
    private val getMerchantQRBitmap: GetMerchantQRBitmap,
    private val communicationApi: CommunicationRepository,
    private val customerRepo: Lazy<CustomerRepo>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(isWhatsAppIntent: Boolean = false): Single<Intent> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            Single.zip(
                findLinkAndMessageFromLinkPay(businessId),
                getMerchantQRBitmap.execute(),
                { message, bitmap -> createSharableIntent(isWhatsAppIntent, message, bitmap) }
            )
        }
    }

    private fun createSharableIntent(
        isWhatsAppIntent: Boolean,
        message: String?,
        bitmapResponse: GetMerchantQRBitmap.BitmapResponse,
    ): Intent {
        val whatsAppIntentBuilder = getWhatsAppIntentBuilder(bitmapResponse, message)
        return if (isWhatsAppIntent) {
            getWhatsAppIntent(whatsAppIntentBuilder).blockingGet()
        } else {
            getSharableIntent(whatsAppIntentBuilder).blockingGet()
        }
    }

    private fun findLinkAndMessageFromLinkPay(businessId: String): Single<String?> {
        return customerRepo.get().getLiveSalesCustomerId(businessId).flatMap { customerId ->
            if (customerId.isNotNullOrBlank()) {
                return@flatMap collectionRepository.get().getCollectionCustomerProfile(customerId, businessId).firstOrError()
                    .map { collectionCustomerProfile ->
                        var reminderText = collectionCustomerProfile.message
                        reminderText =
                            reminderText?.replace(
                                "$" + "message_link",
                                collectionCustomerProfile.message_link.toString()
                            )
                        return@map reminderText
                    }
            }

            return@flatMap Single.just(null)
        }.onErrorReturn {
            // return null message if no
            return@onErrorReturn ""
        }
    }

    private fun throwException(
        throwable: Throwable,
        shareIntentBuilder: ShareIntentBuilder,
    ): Single<Intent> {
        return if (throwable is IntentHelper.NoWhatsAppError)
            getSharableIntent(shareIntentBuilder)
        else
            Single.error(throwable)
    }

    private fun getWhatsAppIntent(whatsAppIntentBuilder: ShareIntentBuilder) =
        communicationApi.goToWhatsApp(whatsAppIntentBuilder).onErrorResumeNext { throwable ->
            return@onErrorResumeNext throwException(throwable, whatsAppIntentBuilder)
        }.doOnError {
            RecordException.recordException(it)
            Timber.e("<< ${it.message}")
        }

    private fun getSharableIntent(whatsAppIntentBuilder: ShareIntentBuilder) =
        communicationApi.goToSharableApp(whatsAppIntentBuilder).onErrorResumeNext { throwable ->
            return@onErrorResumeNext throwException(throwable, whatsAppIntentBuilder)
        }.doOnError {
            RecordException.recordException(it)
            Timber.e("<< ${it.message}")
        }

    private fun getWhatsAppIntentBuilder(it: GetMerchantQRBitmap.BitmapResponse, message: String?): ShareIntentBuilder {
        return ShareIntentBuilder(
            shareText = message,
            imageFrom = ImagePath.ImageUriFromBitMap(
                bitmap = it.bitmap,
                context = context,
                folderName = GetPaymentReminderIntent.FOLDER_NAME,
                imageName = GetPaymentReminderIntent.FILE_NAME
            )
        )
    }
}
