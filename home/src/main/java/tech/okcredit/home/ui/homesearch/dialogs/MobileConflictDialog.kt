package tech.okcredit.home.ui.homesearch.dialogs

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.suppliercredit.Supplier
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import tech.okcredit.home.R

class MobileConflictDialog {
    interface Listener {
        fun onViewClicked()
    }

    companion object {

        fun showCustomerConflict(activity: Activity?, customer: Customer?, listener: Listener?) {
            activity?.let {
                showDialog(activity, customer?.mobile, customer?.description, true, listener)
            }
        }

        fun showSupplierConflict(activity: Activity?, supplier: Supplier?, listener: Listener?) {
            activity?.let {
                showDialog(activity, supplier?.mobile, supplier?.name, false, listener)
            }
        }

        private fun showDialog(
            activity: Activity,
            mobile: String?,
            name: String?,
            isCustomer: Boolean,
            listener: Listener?
        ) {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)

            val layoutInflater = activity.layoutInflater
            val dialogView = layoutInflater.inflate(R.layout.dialog_mobile_conflict, null)
            builder.setView(dialogView)

            val alertDialog = builder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
            }

            val title = dialogView.findViewById<TextView>(R.id.title)
            val viewSupplierButton = dialogView.findViewById<CardView>(R.id.bottom_button)
            val btnTitle = dialogView.findViewById<TextView>(R.id.btn_title)

            if (isCustomer) {
                title.text = activity.getString(R.string.err_mobile_conflict_v2, mobile, name)
                btnTitle.text = activity.getString(R.string.view_customer)
            } else {
                title.text = activity.getString(R.string.err_mobile_conflict_suppliers, mobile, name)
                btnTitle.text = activity.getString(R.string.view_supplier)
            }

            viewSupplierButton.setOnClickListener {
                if (listener != null) {
                    alertDialog.dismiss()
                    listener.onViewClicked()
                }
            }

            if (alertDialog.window != null) {
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            alertDialog.setCancelable(true)

            alertDialog.show()
        }
    }
}
