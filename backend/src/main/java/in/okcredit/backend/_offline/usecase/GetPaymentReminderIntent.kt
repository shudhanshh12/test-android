package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.R
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.backend.utils.QRCodeUtils
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.utils.AbFeatures
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.common.base.Strings
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.joda.time.format.DateTimeFormat
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.getResourcesByLocale
import tech.okcredit.android.base.extensions.getRoundedBitmap
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.DateTimeUtils.currentDateTime
import tech.okcredit.android.base.utils.onErrorIfNotDisposed
import tech.okcredit.android.base.utils.onSuccessIfNotDisposed
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import timber.log.Timber
import java.io.IOException
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.absoluteValue

class GetPaymentReminderIntent @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    val context: Context,
    private val customerRepo: Lazy<CustomerRepo>,
    private val tracker: Lazy<Tracker>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val communicationApi: Lazy<CommunicationRepository>,
    private val iUploadFile: Lazy<IUploadFile>,
    private val ab: Lazy<AbRepository>,
) {

    companion object {
        const val FILE_NAME = "reminder.jpg"
        const val FOLDER_NAME = "reminder_images"

        enum class ReminderMode(val value: String) {
            SMS("sms"),
            WHATSAPP("whatsapp")
        }
    }

    // if reminderMode is null, select reminderMode from customer
    fun execute(
        customerId: String,
        screen: String,
        reminderMode: String?,
        reminderStringsObject: ReminderStringsObject? = null,
        cashbackEligible: Boolean = false,
    ): Single<Intent> {
        return getActiveBusiness.get().execute().firstOrError().flatMap { business ->
            collectionRepository.get().isCollectionActivated().firstOrError().flatMap {
                if (it) {
                    shareForCustomerDestinationAvailable(
                        customerId,
                        screen,
                        reminderMode,
                        reminderStringsObject,
                        cashbackEligible,
                        business,
                    )
                } else {
                    customerDestinationNotAvailablePaymentIntent(
                        customerId,
                        reminderStringsObject,
                        reminderMode,
                        screen,
                        cashbackEligible,
                        business,
                    )
                }
            }.onErrorReturn {
                RecordException.recordException(it)
                customerDestinationNotAvailablePaymentIntent(
                    customerId,
                    reminderStringsObject,
                    reminderMode,
                    screen,
                    cashbackEligible,
                    business,
                ).blockingGet()
            }
        }
    }

    private fun shareForCustomerDestinationAvailable(
        customerId: String,
        screen: String,
        reminderMode: String?,
        reminderStringsObject: ReminderStringsObject? = null,
        cashbackEligible: Boolean = false,
        business: Business,
    ): Single<Intent> {
        val observables = listOf(
            customerRepo.get().getCustomer(customerId, business.id),
            collectionRepository.get().getCollectionCustomerProfile(customerId, business.id)
        )
        return Observable.combineLatest(observables) {
            val customer = it[0] as Customer
            val collectionCustomerProfile = it[1] as CollectionCustomerProfile
            Response(business, customer, collectionCustomerProfile)
        }.firstOrError().flatMap { response ->
            if (response.collectionCustomerProfile != null) {
                customerDestinationAvailablePaymentIntent(
                    response,
                    reminderMode,
                    screen,
                    response.collectionCustomerProfile,
                    cashbackEligible = cashbackEligible,
                )
            } else {
                customerDestinationNotAvailablePaymentIntent(
                    customerId,
                    reminderStringsObject,
                    reminderMode,
                    screen,
                    cashbackEligible = cashbackEligible,
                    business,
                )
            }
        }
    }

    private fun customerDestinationAvailablePaymentIntent(
        response: Response,
        reminderMode: String?,
        screen: String,
        collectionCustomerProfile: CollectionCustomerProfile,
        cashbackEligible: Boolean = false,
    ): Single<Intent> {
        Timber.d("customerDestinationAvailablePaymentIntent start")
        return createCollectionReminderClusterBitmap(
            context,
            response.customer,
            response.business,
            response.collectionCustomerProfile,
            cashbackEligible = cashbackEligible,
        ).flatMap { bitmap ->
            return@flatMap shareBitmap(
                bitmap = bitmap,
                response = response,
                reminderMode = reminderMode,
                screen = screen,
                collectionCustomerProfile = collectionCustomerProfile,
                cashbackEligible = cashbackEligible,
            )
        }
    }

    private fun shareBitmap(
        bitmap: BitmapResponse,
        response: Response,
        reminderMode: String?,
        screen: String,
        collectionCustomerProfile: CollectionCustomerProfile,
        cashbackEligible: Boolean,
    ): Single<Intent> {
        Timber.d("shareBitmap start")
        val customerLang = response.customer.lang ?: ""
        val amountText = if (response.customer.balanceV2 >= 0) context.getResourcesByLocale(customerLang)
            .getString(R.string.advance) else context.getResourcesByLocale(customerLang).getString(R.string.due)

        var reminderText = response.collectionCustomerProfile?.message
        reminderText = reminderText?.replace(
            "$" + "current_balance",
            (abs(response.customer.balanceV2) / 100.0).toString() + " " + amountText
        )
        reminderText =
            reminderText?.replace("$" + "message_link", response.collectionCustomerProfile?.message_link.toString())

        var finalReminderMode = bitmap.customer.reminderMode
        if (reminderMode.isNullOrEmpty().not()) {
            finalReminderMode = reminderMode
        }
        if (!finalReminderMode.isNullOrEmpty() && finalReminderMode == "sms") {
            tracker.get().trackSendReminder(
                PropertyValue.SMS,
                bitmap.customer.id,
                "",
                screen,
                PropertyValue.CUSTOMER,
                bitmap.customer.mobile,
                "",
                balance = amountText
            )
            tracker.get().trackSendPaymentReminder(
                PropertyValue.SMS,
                screen,
                false,
                bitmap.customer.id,
                ""
            )
            val shareIntentBuilder = ShareIntentBuilder(
                shareText = reminderText,
                phoneNumber = bitmap.customer.mobile
            )
            return communicationApi.get().goToSms(shareIntentBuilder).doOnError {
                RecordException.recordException(it)
                Timber.e("<< ${it.message}")
            }
        } else if (bitmap.collectionCustomerProfile != null && collectionCustomerProfile.show_image) {
            tracker.get().trackSendReminder(
                PropertyValue.WHATSAPP,
                bitmap.customer.id,
                "",
                screen,
                PropertyValue.CUSTOMER,
                bitmap.customer.mobile,
                "",
                balance = amountText,
                cashbackMsgShown = cashbackEligible,
                dueAmount = response.customer.balanceV2.absoluteValue.toString(),
            )
            tracker.get().trackSendPaymentReminder(
                PropertyValue.WHATSAPP,
                screen,
                true,
                bitmap.customer.id,
                "",
                cashbackMsgShown = cashbackEligible,
                dueAmount = response.customer.balanceV2.absoluteValue.toString(),
            )
            val whatsappIntentBuilder = ShareIntentBuilder(
                shareText = reminderText,
                phoneNumber = bitmap.customer.mobile,
                imageFrom = ImagePath.ImageUriFromBitMap(
                    bitmap.bitmap,
                    context,
                    FOLDER_NAME,
                    FILE_NAME
                )
            )
            Timber.d("shareBitmap goToWhatsApp")
            return communicationApi.get().goToWhatsApp(whatsappIntentBuilder).onErrorResumeNext {
                if (it is IntentHelper.NoWhatsAppError) {
                    Timber.d("shareBitmap NoWhatsAppError")
                    communicationApi.get().goToSms(whatsappIntentBuilder)
                } else
                    Single.error(it)
            }.doOnError {
                RecordException.recordException(it)
                Timber.e("<< ${it.message}")
            }
        } else {
            tracker.get().trackSendReminder(
                PropertyValue.WHATSAPP,
                bitmap.customer.id,
                "",
                screen,
                PropertyValue.CUSTOMER,
                bitmap.customer.mobile,
                "",
                balance = amountText,
                cashbackMsgShown = cashbackEligible,
            )
            tracker.get().trackSendPaymentReminder(
                PropertyValue.WHATSAPP,
                screen,
                true,
                bitmap.customer.id,
                "",
                cashbackMsgShown = cashbackEligible,
            )

            val whatsappIntentBuilder = ShareIntentBuilder(
                shareText = reminderText,
                phoneNumber = bitmap.customer.mobile
            )
            return communicationApi.get().goToWhatsApp(whatsappIntentBuilder).onErrorResumeNext {
                if (it is IntentHelper.NoWhatsAppError)
                    return@onErrorResumeNext communicationApi.get().goToSms(whatsappIntentBuilder)
                else
                    return@onErrorResumeNext Single.error(it)
            }.doOnError {
                RecordException.recordException(it)
                Timber.e("<< ${it.message}")
            }
        }
    }

    private fun getMerchantDisplayName(name: String): String {
        if (name.length > 30) {
            return name.substring(0, 30) + "..."
        }
        return name
    }

    private fun customerDestinationNotAvailablePaymentIntent(
        customerId: String,
        reminderStringsObject: ReminderStringsObject?,
        reminderMode: String?,
        screen: String,
        cashbackEligible: Boolean = false,
        business: Business,
    ): Single<Intent> {
        Timber.d("customerDestinationNotAvailablePaymentIntent start")
        return customerRepo.get().getCustomer(customerId, business.id)
            .firstOrError().flatMap { customer ->
                Timber.d("customerDestinationNotAvailablePaymentIntent combineLatest start")
                val reminderText: String
                val language = customer.lang ?: ""
                if (reminderStringsObject != null) {

                    reminderText =
                        if (customer.accountUrl.isNullOrBlank() &&
                            ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = business.id).blockingFirst()
                        ) {
                            context.getResourcesByLocale(language).getString(
                                R.string.payment_reminder_text_without_account_url,
                                CurrencyUtil.formatV2(customer.balanceV2),
                                business.name
                            )
                        } else {
                            context.getResourcesByLocale(language).getString(
                                reminderStringsObject.paymentReminderText,
                                CurrencyUtil.formatV2(customer.balanceV2),
                                business.name,
                                customer.accountUrl
                            )
                        }
                } else {
                    reminderText = if (customer.accountUrl.isNullOrBlank()) {
                        context.getResourcesByLocale(language).getString(
                            R.string.payment_reminder_text_without_account_url,
                            CurrencyUtil.formatV2(customer.balanceV2),
                            business.name
                        )
                    } else {
                        context.getResourcesByLocale(language).getString(
                            R.string.payment_reminder_text,
                            CurrencyUtil.formatV2(customer.balanceV2),
                            business.name,
                            customer.accountUrl
                        )
                    }
                }
                return@flatMap createCollectionReminderClusterBitmap(
                    context,
                    customer,
                    business,
                    null,
                    cashbackEligible = cashbackEligible,
                ).flatMap { bitmapResponse ->
                    Timber.d("createCollectionReminderClusterBitmap bitmapResponse start")
                    shareDestinationNotAvailable(
                        bitmapResponse = bitmapResponse,
                        reminderMode = reminderMode,
                        screen = screen,
                        reminderText = reminderText,
                        cashbackEligible = cashbackEligible,
                    )
                }
            }
    }

    private fun shareDestinationNotAvailable(
        bitmapResponse: BitmapResponse,
        reminderMode: String?,
        screen: String,
        reminderText: String,
        cashbackEligible: Boolean,
    ): Single<Intent> {
        Timber.d("shareDestinationNotAvailable start")
        var finalReminderMode = bitmapResponse.customer.reminderMode
        if (reminderMode.isNullOrEmpty().not()) {
            finalReminderMode = reminderMode
        }

        val amountText = if (bitmapResponse.customer.balanceV2 >= 0) "advance" else "due"

        if (!finalReminderMode.isNullOrEmpty() && finalReminderMode == "sms") {
            tracker.get().trackSendReminder(
                PropertyValue.SMS,
                bitmapResponse.customer.id,
                "",
                screen,
                PropertyValue.CUSTOMER,
                bitmapResponse.customer.mobile,
                "",
                balance = amountText
            )

            val whatsappIntentBuilder = ShareIntentBuilder(
                shareText = reminderText,
                phoneNumber = bitmapResponse.customer.mobile
            )
            return communicationApi.get().goToSms(whatsappIntentBuilder)
        }
        tracker.get().trackSendReminder(
            PropertyValue.WHATSAPP,
            bitmapResponse.customer.id,
            "",
            screen,
            PropertyValue.CUSTOMER,
            bitmapResponse.customer.mobile,
            "",
            balance = amountText,
            cashbackMsgShown = cashbackEligible,
            dueAmount = bitmapResponse.customer.balanceV2.absoluteValue.toString(),
        )
        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = reminderText,
            phoneNumber = bitmapResponse.customer.mobile,
            imageFrom = ImagePath.ImageUriFromBitMap(bitmapResponse.bitmap, context, FOLDER_NAME, FILE_NAME)
        )

        return communicationApi.get().goToWhatsApp(whatsappIntentBuilder)
    }

    private fun createCollectionReminderClusterBitmap(
        context: Context,
        customer: Customer,
        business: Business,
        collectionCustomerProfile: CollectionCustomerProfile?,
        reminderStringsObject: ReminderStringsObject? = null,
        cashbackEligible: Boolean = false,
    ): Single<BitmapResponse> {
        return Single.create { emitter ->
            try {
                Timber.d("createCollectionReminderClusterBitmap start")
                val language = customer.lang ?: "en"
                val cluster = LayoutInflater.from(ContextThemeWrapper(context, R.style.Base_OKCTheme))
                    .inflate(R.layout.payment_collection_reminder_layout, null)

                cluster.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                cluster.layout(0, 0, cluster.measuredWidth, cluster.measuredHeight)

                val rootLayout = cluster.findViewById(R.id.rootLayout) as ConstraintLayout
                val merchantName = cluster.findViewById(R.id.tv_merchant_name) as TextView
                val reminderBalance = cluster.findViewById(R.id.tv_amount) as TextView
                val phoneNumberCustomer = cluster.findViewById(R.id.tv_mobile) as TextView
                val payViaOkCredit = cluster.findViewById(R.id.pay_via_okcredit) as TextView
                val qrCodeImage = cluster.findViewById(R.id.iv_qr_code) as ImageView
                val date = cluster.findViewById(R.id.tv_due_date) as TextView
                val profileImage = cluster.findViewById(R.id.iv_profile_image) as ImageView
                val tvReminderHeader = cluster.findViewById(R.id.tv_reminder_header) as TextView
                val tvOkcVerified = cluster.findViewById(R.id.tv_okc_verified) as TextView
                val imageViewCashback = cluster.findViewById(R.id.imageViewCashback) as ImageView
                val textCashback = cluster.findViewById(R.id.textCashback) as TextView

                imageViewCashback.isVisible = cashbackEligible
                textCashback.isVisible = cashbackEligible

                payViaOkCredit.text = context.getResourcesByLocale(language).getString(R.string.pay_via_okcredit)
                tvReminderHeader.text = context.getResourcesByLocale(language).getString(R.string.payment_reminder)
                tvOkcVerified.text = context.getResourcesByLocale(language).getString(R.string.verified_by_okc)

                val dtfOut = DateTimeFormat.forPattern("dd MMM").withLocale(LocaleManager.englishLocale)

                if (reminderStringsObject != null) {
                    date.text = context.getResourcesByLocale(language)
                        .getString(reminderStringsObject.dueOn, dtfOut.print(currentDateTime()))
                } else {
                    date.text = context.getResourcesByLocale(language)
                        .getString(R.string.due_as_on, dtfOut.print(currentDateTime()))
                }

                if (customer.isLiveSales.not()) {
                    reminderBalance.visibility = View.VISIBLE
                    phoneNumberCustomer.visibility = View.VISIBLE
                    reminderBalance.text = "₹" + CurrencyUtil.formatV2(customer.balanceV2)
                    if (customer.mobile == null) phoneNumberCustomer.visibility = View.GONE
                    if (reminderStringsObject != null) {
                        phoneNumberCustomer.text = context.getResourcesByLocale(language)
                            .getString(reminderStringsObject.toMobile, customer.mobile)
                    } else {
                        phoneNumberCustomer.text =
                            context.getResourcesByLocale(language).getString(R.string.to_mobile, customer.mobile)
                    }
                } else {
                    reminderBalance.visibility = View.GONE
                    phoneNumberCustomer.visibility = View.GONE
                }

                merchantName.text = getMerchantDisplayName(business.name)

                if (collectionCustomerProfile == null) {
                    qrCodeImage.visibility = View.GONE
                    payViaOkCredit.visibility = View.GONE
                } else {
                    val qrIntent = QrCodeBuilder.getQrCode(
                        qrIntent = collectionCustomerProfile.qr_intent,
                        currentBalance = customer.balanceV2,
                        lastPayment = customer.lastPayment,
                    )
                    qrCodeImage.setQrCode(qrIntent, context, 140)
                    qrCodeImage.visibility = View.VISIBLE
                    payViaOkCredit.visibility = View.VISIBLE
                }

                var isImageFail = false

                if (!Strings.isNullOrEmpty(business.profileImage)) {
                    try {
                        var image: Bitmap? = null
                        val filePath =
                            iUploadFile.get().getMerchantImageFile(business.profileImage!!).blockingGet()?.path

                        try {
                            image = BitmapFactory.decodeFile(filePath)
                        } catch (e: Exception) {
                        }

                        // Cached merchant image is not available if image is null
                        if (image == null) {
                            try {
                                val url = URL(business.profileImage)
                                image = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                                iUploadFile.get().saveMerchantImageFile(business.profileImage!!).subscribe()
                            } catch (e: IOException) {
                                isImageFail = true
                            }
                        }

                        if (!isImageFail) {
                            image?.let {
                                profileImage.setImageBitmap(it.getRoundedBitmap())
                            }
                        }
                    } catch (e: IOException) {
                        isImageFail = true
                    }
                }

                if (isImageFail || Strings.isNullOrEmpty(business.profileImage)) {
                    val defaultPic = TextDrawable
                        .builder()
                        .buildRound(
                            business.name.substring(0, 1).uppercase(Locale.getDefault()),
                            ColorGenerator.MATERIAL.getColor(business.name)
                        )

                    val bitmap = defaultPic.toBitmap(72.convertToPx(), 72.convertToPx(), null)
                    profileImage.setImageBitmap(bitmap)
                }

                rootLayout.isDrawingCacheEnabled = true
                rootLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                rootLayout.layout(0, 0, rootLayout.measuredWidth, rootLayout.measuredHeight)
                rootLayout.buildDrawingCache(true)
                val bm = rootLayout.drawingCache

                emitter.onSuccessIfNotDisposed(BitmapResponse(bm, business, customer, collectionCustomerProfile))
            } catch (e: Exception) {
                emitter.onErrorIfNotDisposed(e)
            }
        }
    }

    private fun Int.convertToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun ImageView.setQrCode(upiVpa: String, context: Context, width: Int) {
        val bitmap = QRCodeUtils.getBitmap(upiVpa, context, width)
        if (bitmap != null) {
            this.setImageBitmap(bitmap)
        }
    }

    data class Response(
        val business: Business,
        val customer: Customer,
        val collectionCustomerProfile: CollectionCustomerProfile?,
    )

    data class BitmapResponse(
        val bitmap: Bitmap,
        val business: Business,
        val customer: Customer,
        val collectionCustomerProfile: CollectionCustomerProfile?,
    )

    @Keep
    @Parcelize
    data class ReminderStringsObject(
        @StringRes val paymentReminderText: Int = 0,
        @StringRes val toMobile: Int = 0,
        @StringRes val dueOn: Int = 0,
    ) : Parcelable
}
