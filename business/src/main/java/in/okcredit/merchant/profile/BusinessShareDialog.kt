package `in`.okcredit.merchant.profile

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.merchant.R
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog

object BusinessShareDialog {

    interface Listener {
        fun onBusinessCardShare(bitmap: Bitmap)
    }

    fun showBusinessCard(context: Activity, business: Business, listener: Listener) {

        val layoutInflater = context.layoutInflater
        val dialog = BottomSheetDialog(context)
        val bottomSheet = layoutInflater.inflate(R.layout.merchant_business_sheet, null)
        dialog.setContentView(bottomSheet)

        initViews(
            dialog,
            business,
            listener
        )

        dialog.show()
    }

    private fun initViews(dialog: BottomSheetDialog, business: Business, listener: Listener) {

        val tvBusinessName = dialog.findViewById<TextView>(R.id.profile_name)
        val tvMobile = dialog.findViewById<TextView>(R.id.phone_number)
        val tvAddress = dialog.findViewById<TextView>(R.id.address)
        val tvAbout = dialog.findViewById<TextView>(R.id.about)
        val tvEmail = dialog.findViewById<TextView>(R.id.email)
        val ivEmail = dialog.findViewById<ImageView>(R.id.email_icon)
        val tvPersonName = dialog.findViewById<TextView>(R.id.name)
        val ivPersonName = dialog.findViewById<ImageView>(R.id.contact_person_icon)
        val ivProfileImage = dialog.findViewById<ImageView>(R.id.profile_image)
        val cvSend = dialog.findViewById<CardView>(R.id.send)
        val rootLayout = dialog.findViewById<CardView>(R.id.rootLayout)

        tvBusinessName?.text = business.name
        tvMobile?.text = business.mobile

        if (business.address.isNullOrBlank()) {
            tvAddress?.visibility = View.GONE
        } else {
            tvAddress?.visibility = View.VISIBLE
            tvAddress?.text = business.address
        }

        if (business.about.isNullOrBlank()) {
            tvAbout?.visibility = View.GONE
        } else {
            tvAbout?.visibility = View.VISIBLE
            tvAbout?.text = "( ${business.about} )"
        }

        if (business.email.isNullOrBlank()) {
            tvEmail?.visibility = View.GONE
            ivEmail?.visibility = View.GONE
        } else {
            tvEmail?.text = business.email
            tvEmail?.visibility = View.VISIBLE
            ivEmail?.visibility = View.VISIBLE
        }

        if (business.contactName.isNullOrBlank()) {
            tvPersonName?.visibility = View.GONE
            ivPersonName?.visibility = View.GONE
        } else {
            tvPersonName?.text = business.contactName
            tvPersonName?.visibility = View.VISIBLE
            ivPersonName?.visibility = View.VISIBLE
        }

        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                business.name.substring(0, 1).toUpperCase(),
                ColorGenerator.MATERIAL.getColor(business.name)
            )

        ivProfileImage?.let {
            GlideApp
                .with(dialog.context)
                .load(business.profileImage)
                .placeholder(defaultPic)
                .error(defaultPic)
                .fallback(defaultPic)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(it)
        }

        cvSend?.setOnClickListener {
            listener.onBusinessCardShare(
                getBitmap(
                    rootLayout
                )
            )
            dialog.dismiss()
        }
    }

    private fun getBitmap(rootLayout: CardView?): Bitmap {
        // Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(rootLayout!!.width, rootLayout!!.height, Bitmap.Config.ARGB_8888)
        // Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        // Get the view's background
        val bgDrawable = rootLayout.background
        if (bgDrawable != null)
        // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        else
        // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        rootLayout?.draw(canvas)
        // return the bitmap
        return returnedBitmap
    }
}
