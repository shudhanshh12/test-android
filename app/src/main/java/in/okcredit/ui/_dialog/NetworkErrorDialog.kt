package `in`.okcredit.ui._dialog

import `in`.okcredit.R
import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import tech.okcredit.android.base.extensions.isConnectedToInternet

class NetworkErrorDialog {
    interface Listener {
        fun onNetworkOk()
        fun onCancel()
    }

    private var message: String? = null

    constructor() {}
    constructor(message: String?) {
        this.message = message
    }

    fun show(activity: Activity, listener: Listener?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(false)
        val layoutInflater = activity.layoutInflater
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_no_internet, null)
        builder.setView(dialogView)
        if (!TextUtils.isEmpty(message)) {
            val msg = dialogView.findViewById<TextView>(R.id.message)
            msg.text = message
        }
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        if (!activity.isFinishing) {
            alertDialog.show()
        }
        val tryAgain = dialogView.findViewById<Button>(R.id.tryAgain)
        val cancel = dialogView.findViewById<Button>(R.id.cancel)
        tryAgain.setOnClickListener { v: View? ->
            if (activity.isConnectedToInternet()) {
                listener?.onNetworkOk()
                alertDialog.dismiss()
            }
        }
        cancel.setOnClickListener { v: View? ->
            listener?.onCancel()
            alertDialog.dismiss()
        }
        return alertDialog
    }
}
