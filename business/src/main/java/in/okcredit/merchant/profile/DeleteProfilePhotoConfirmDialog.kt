package `in`.okcredit.merchant.profile

import `in`.okcredit.merchant.merchant.R
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.material.card.MaterialCardView

class DeleteProfilePhotoConfirmDialog {

    interface Listener {
        fun onDeletePhoto()
    }

    companion object {

        fun show(activity: Activity, listener: Listener): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_photo_confirm, null)

            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val mvDelete = dialogView.findViewById<MaterialCardView>(R.id.mvDelete)
            val mvCancel = dialogView.findViewById<MaterialCardView>(R.id.mvCancel)

            mvCancel.setOnClickListener {
                alertDialog.dismiss()
            }
            mvDelete.setOnClickListener {
                listener.onDeletePhoto()
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
            val dialogWindowWidth = (displayWidth * 0.8f)
            layoutParams.width = dialogWindowWidth.toInt()
            alertDialog.window?.attributes = layoutParams

            return alertDialog
        }
    }
}
