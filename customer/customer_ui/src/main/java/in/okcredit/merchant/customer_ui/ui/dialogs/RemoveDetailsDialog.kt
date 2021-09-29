package `in`.okcredit.merchant.customer_ui.ui.dialogs

import `in`.okcredit.merchant.customer_ui.R
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class RemoveDetailsDialog {
    interface RemoveWarningDialogListener {
        fun onRemoveClicked()
    }

    companion object {

        fun showRemoveDetailsDialog(activity: Activity?, listener: RemoveWarningDialogListener?) {
            activity?.let {
                showDialog(activity, listener)
            }
        }

        private fun showDialog(
            activity: Activity,
            listener: RemoveWarningDialogListener?
        ) {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_remove_details, null)
            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val tvCancel = dialogView.findViewById<TextView>(R.id.cancel_tv)
            val tvRemove = dialogView.findViewById<TextView>(R.id.remove_tv)

            tvRemove.setOnClickListener {
                listener?.onRemoveClicked()
            }

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialog.setCancelable(true)

            alertDialog.show()

            tvCancel.setOnClickListener {
                alertDialog.dismiss()
            }
        }
    }
}
