package `in`.okcredit.ui._dialog

import `in`.okcredit.R
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView

class TxSmsDialog {
    interface Listener {
        fun onButtonClicked()
    }

    fun show(activity: Activity, isSwitchOn: Boolean, listener: Listener?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        val layoutInflater = activity.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_tx_sms, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        if (!activity.isFinishing) {
            alertDialog.show()
        }
        val sumbit: CardView = dialogView.findViewById(R.id.submit)
        val btnTitle = dialogView.findViewById<TextView>(R.id.btn_title)
        val content = dialogView.findViewById<TextView>(R.id.content)
        if (isSwitchOn) {
            content.text = Html.fromHtml(activity.getString(R.string.start_switching_on))
            btnTitle.setText(R.string.switch_on)
        } else {
            content.text = Html.fromHtml(activity.getString(R.string.after_switching_off))
            btnTitle.setText(R.string.switch_off)
        }
        sumbit.setOnClickListener { v: View? ->
            if (listener != null) {
                listener.onButtonClicked()
                alertDialog.dismiss()
            }
        }
        if (alertDialog.window != null) {
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        alertDialog.setCancelable(true)
        return alertDialog
    }
}
