package tech.okcredit.home.dialogs

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.setQrCode
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.QrCodeBuilder
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.merchant.contract.Business
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import org.joda.time.DateTime
import org.joda.time.Duration
import tech.okcredit.home.R

object LiveSalesQRDialog {

    private lateinit var imageLoader: IImageLoader
    private lateinit var tracker: Tracker

    @JvmStatic
    fun inject(imageLoader: IImageLoader, tracker: Tracker) {
        this.imageLoader = imageLoader
        this.tracker = tracker
    }

    @SuppressLint("SetTextI18n")
    @JvmStatic
    fun show(
        context: Context,
        customer: Customer,
        collectionCustomerProfile: CollectionCustomerProfile,
        business: Business?
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.livesales_dialog, null)
        builder.setView(dialogView)
        dialogView.setPadding(0, 0, 0, 0)
        val alertDialog = builder.create()

        val timeStart = DateTime.now()

        alertDialog.setOnCancelListener {
            val diff = Duration(timeStart, DateTime.now()).standardSeconds
            tracker.trackStopLiveSale(diff.toString())
        }

        val qrImage = dialogView.findViewById<ImageView>(R.id.qr_image)
        val profileImage = dialogView.findViewById<ImageView>(R.id.profile_image)
        val profileImageContainer = dialogView.findViewById<RelativeLayout>(R.id.profile_image_container)

        if (business?.profileImage.isNullOrEmpty()) {
            profileImageContainer.visibility = View.GONE
        } else {
            profileImageContainer.visibility = View.VISIBLE
            imageLoader.context(context)
                .load(business?.profileImage)
                .placeHolder(ContextCompat.getDrawable(context, R.drawable.ic_account_125dp)!!)
                .scaleType(IImageLoader.CIRCLE_CROP)
                .into(profileImage)
                .build()
        }

        val deviceWidth = context.resources.displayMetrics.widthPixels

        val qrIntent = QrCodeBuilder.getQrCode(
            qrIntent = collectionCustomerProfile.qr_intent,
            currentBalance = customer.balanceV2,
            lastPayment = customer.lastPayment,
        )
        qrImage.setQrCode(qrIntent, context, (deviceWidth - 0))
        return alertDialog
    }
}
