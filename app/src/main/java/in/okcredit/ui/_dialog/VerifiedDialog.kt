package `in`.okcredit.ui._dialog

import `in`.okcredit.R
import `in`.okcredit.fileupload._id.GlideApp
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.View
import android.view.View.SCALE_X
import android.view.View.SCALE_Y
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class VerifiedDialog {

    companion object {

        fun show(activity: Activity, profileUrl: String?): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_verified_merchant, null)

            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val gotIt = dialogView.findViewById<CardView>(R.id.fbGotIt)
            val ivProfile = dialogView.findViewById<ImageView>(R.id.ivProfilePhoto)
            val ivRegistered = dialogView.findViewById<ImageView>(R.id.ivRegistered)
            var animate = false

            if (!profileUrl.isNullOrBlank()) {
                animate = true
                GlideApp
                    .with(activity)
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_account_125dp)
                    .error(R.drawable.ic_account_125dp)
                    .fallback(R.drawable.ic_account_125dp)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(ivProfile)
            }
            gotIt.setOnClickListener {
                alertDialog.dismiss()
            }

            if (alertDialog.window != null) {
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            alertDialog.setCancelable(true)

            // setting dialog width to 70% of the screen width
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            val displayWidth = displayMetrics.widthPixels
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(alertDialog.window?.attributes)
            val dialogWindowWidth = (displayWidth * 0.7f)
            layoutParams.width = dialogWindowWidth.toInt()
            alertDialog.window?.attributes = layoutParams

            // animation
            if (animate) {
                val scaleDownX = ObjectAnimator.ofFloat(ivRegistered, SCALE_X, 1.5f, 1.0f)
                val scaleDownY = ObjectAnimator.ofFloat(ivRegistered, SCALE_Y, 1.5f, 1.0f)
                val rotate = ObjectAnimator.ofFloat(ivRegistered, View.ROTATION, 0f, 360f)

                val animationSet = AnimatorSet()
                animationSet.playTogether(scaleDownX, scaleDownY, rotate)

                animationSet.startDelay = 200
                animationSet.duration = 800
                animationSet.interpolator = AccelerateDecelerateInterpolator()
                animationSet.start()
            }

            return alertDialog
        }
    }
}
