package tech.okcredit.home.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import tech.okcredit.home.R

class AccountStatementTutorialDialog {
    interface Listener {
        fun onSubmitClicked()
        fun onCancelClicked()
    }

    companion object {

        public fun show(activity: Activity, listener: Listener?): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_account_statement_tutorial, null)
            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val btnOk = dialogView.findViewById<CardView>(R.id.btn_ok)
            val btnCancel = dialogView.findViewById<TextView>(R.id.btn_cancel)

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
