package `in`.okcredit.onboarding.enterotp.usecase

import `in`.okcredit.onboarding.R
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class WhatsappOptInDialog {
    interface Listener {
        fun onSubmitClicked()
        fun onCancelClicked()
    }

    companion object {

        public fun show(activity: Activity, mobile: String, isDetailed: Boolean, listener: Listener?): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_whatsapp_optin, null)
            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val btnOk = dialogView.findViewById<TextView>(R.id.btn_ok)
            val btnCancel = dialogView.findViewById<TextView>(R.id.btn_cancel)
            val title = dialogView.findViewById<TextView>(R.id.title)
            val desc = dialogView.findViewById<TextView>(R.id.desc)

            if (isDetailed) {
                desc.text = activity.getString(R.string.allow_us_to_communicate, mobile)
            } else {
                title.text = activity.getString(R.string.allow_us_to_communicate_short, mobile)
                desc.visibility = View.GONE
            }

            btnOk.setOnClickListener { v ->
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onSubmitClicked()
                }
            }

            btnCancel.setOnClickListener { v ->
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onCancelClicked()
                }
            }

            if (alertDialog.window != null) {
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            alertDialog.setCancelable(true)

            return alertDialog
        }
    }
}
