package tech.okcredit.base.permission

import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import tech.okcredit.android.base.R

object PermissionDialog {
    interface Listener {
        fun onOpenSetting()
    }

    fun show(activity: FragmentActivity, listener: Listener?): AlertDialog {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)

        val layoutInflater = activity.layoutInflater
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_perimission, null)
        builder.setView(dialogView)

        val setting = dialogView.findViewById<CardView>(R.id.setting)
        val cancel = dialogView.findViewById<CardView>(R.id.cancel)

        val alertDialog = builder.create()
        setting.setOnClickListener { v ->
            listener?.onOpenSetting()
            alertDialog.dismiss()
        }
        cancel.setOnClickListener { v -> alertDialog.dismiss() }
        alertDialog.show()

        return alertDialog
    }
}
