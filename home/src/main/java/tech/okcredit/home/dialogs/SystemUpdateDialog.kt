package tech.okcredit.home.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import tech.okcredit.home.R

class SystemUpdateDialog {
    interface Listener {
        fun onOkClicked()
        fun onCancelClicked()
    }

    companion object {

        fun show(activity: Activity, listener: Listener?): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_system_update, null)
            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val btnOk = dialogView.findViewById<TextView>(R.id.ok)
            val btnCancel = dialogView.findViewById<TextView>(R.id.cancel)
            val txTitle = dialogView.findViewById<TextView>(R.id.title)
            val txSubTitle = dialogView.findViewById<TextView>(R.id.sub_title)

            btnOk.setOnClickListener { v ->
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onOkClicked()
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
