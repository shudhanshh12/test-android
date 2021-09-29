package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend.utils.QRCodeUtils
import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection_ui.R
import `in`.okcredit.merchant.contract.GetActiveBusiness
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import io.reactivex.Single
import tech.okcredit.android.base.utils.onErrorIfNotDisposed
import tech.okcredit.android.base.utils.onSuccessIfNotDisposed
import javax.inject.Inject

class GetMerchantQRBitmap @Inject constructor(
    private val getActiveBusiness: GetActiveBusiness,
    val context: Context,
    private val collectionRepository: CollectionRepository,
) {

    companion object {
        const val UPI = "upi"
        const val PAY = "pay"
        const val PAYMENT_ADDRESS = "pa"
        const val PAYMENT_NAME = "pn"
    }

    fun execute(): Single<BitmapResponse> {
        return getActiveBusiness.execute().firstOrError().flatMap { business ->
            collectionRepository.getCollectionMerchantProfile(business.id)
                .firstOrError()
                .map { collectionMerchantProfile ->
                    Response(
                        business,
                        collectionMerchantProfile
                    )
                }
        }.flatMap {
            getSharableMerchantQR(context, it.business, it.collectionMerchantProfile)
        }
    }

    private fun getSharableMerchantQR(
        context: Context,
        business: `in`.okcredit.merchant.contract.Business,
        collectionMerchantProfile: CollectionMerchantProfile,
    ): Single<BitmapResponse> {

        return Single.create { emitter ->
            try {
                val cluster = LayoutInflater.from(ContextThemeWrapper(context, R.style.Base_OKCTheme))
                    .inflate(R.layout.shareable_qr_layout, null)

                cluster.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                cluster.layout(0, 0, cluster.measuredWidth, cluster.measuredHeight)

                val rootLayout = cluster.findViewById(R.id.root_layout) as LinearLayout
                val merchantName = cluster.findViewById(R.id.tv_merchant_name) as TextView
                val merchantMobile = cluster.findViewById(R.id.merchant_mobile) as TextView
                val qrCodeImage = cluster.findViewById(R.id.qr_image) as ImageView
                val checkIcon = cluster.findViewById(R.id.check_icon) as ImageView
                val okcreditLogo = cluster.findViewById(R.id.okcredit_logo) as ImageView
                val upiLogos = cluster.findViewById(R.id.upi_logos) as ImageView
                val divider = cluster.findViewById(R.id.divider) as View

                merchantMobile.text = business.mobile
                checkIcon.setImageResource(R.drawable.ic_success)
                okcreditLogo.setImageResource(R.drawable.ic_okcredit_logo)
                upiLogos.setImageResource(R.drawable.ic_upi_logos)
                divider.setBackgroundResource(R.color.grey300)

                val merchantCollectionName = getMerchantCollectionProfileName(collectionMerchantProfile, business)
                merchantName.text = merchantCollectionName

                val currentUpiVpa = collectionMerchantProfile.merchant_vpa ?: ""

                val builder = Uri.Builder()
                builder.scheme(UPI)
                    .authority(PAY)
                    .appendQueryParameter(PAYMENT_ADDRESS, currentUpiVpa)
                    .appendQueryParameter(PAYMENT_NAME, merchantCollectionName)

                val upiQrCodeLink = builder.build().toString().replace("%40", "@")

                QRCodeUtils.encodeAsBitmap(upiQrCodeLink, context, 240)?.let {
                    qrCodeImage.setImageBitmap(it)
                }

                val bitmap = createBitmapFromView(rootLayout)

                emitter.onSuccessIfNotDisposed(
                    BitmapResponse(
                        bitmap,
                        business,
                        collectionMerchantProfile
                    )
                )
            } catch (e: Exception) {
                emitter.onErrorIfNotDisposed(e)
            }
        }
    }

    private fun getMerchantCollectionProfileName(
        collectionMerchantProfile: CollectionMerchantProfile,
        business: `in`.okcredit.merchant.contract.Business,
    ): String? {
        return if (collectionMerchantProfile.name.isNullOrBlank()
            .not()
        ) collectionMerchantProfile.name else business.name
    }

    fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                0, View.MeasureSpec.UNSPECIFIED
            ),
            View.MeasureSpec.makeMeasureSpec(
                0, View.MeasureSpec.UNSPECIFIED
            )
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val background = view.background
        background?.draw(canvas)
        view.draw(canvas)
        return bitmap
    }

    data class Response(
        val business: `in`.okcredit.merchant.contract.Business,
        val collectionMerchantProfile: CollectionMerchantProfile,
    )

    data class BitmapResponse(
        val bitmap: Bitmap,
        val business: `in`.okcredit.merchant.contract.Business,
        val collectionMerchantProfile: CollectionMerchantProfile,
    )
}
